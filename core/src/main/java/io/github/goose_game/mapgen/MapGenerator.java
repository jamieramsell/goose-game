package io.github.goose_game.mapgen;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

/**
 * Generates a random {@link TileMap} from a seed.
 *
 * <p>Generation is deterministic: the same seed always produces the same map, because all
 * randomness is drawn from a single {@link Random} created from that seed.
 *
 * <p>A map is built from blocks of uniform terrain, then places spawn and escape points (each
 * surrounded by a 3x3 patch of grass). Blocks next to water have a boosted chance of being water.
 */
public class MapGenerator {

  /** The width and height of a single tile, in pixels. */
  public static final int TILE_SIZE = 32;

  /** The width and height of each terrain block, in pixels. */
  public static final int BLOCK_SIZE = 128;

  /** The number of terrain blocks along each side of the map. */
  public static final int BLOCKS_PER_SIDE = 64;

  /**
   * The width and height of the (square) map, in pixels. Must be a multiple of {@link #TILE_SIZE}.
   */
  public static final int MAP_SIZE = BLOCK_SIZE * BLOCKS_PER_SIDE;

  private final TileDistribution distribution;

  /** Creates a generator that uses {@link TileDistribution#DEFAULT_DISTRIBUTION}. */
  public MapGenerator() {
    distribution = new TileDistribution(TileDistribution.DEFAULT_DISTRIBUTION);
  }

  /**
   * Generates a map for the given seed.
   *
   * @param seed the seed for the random number generator; equal seeds yield equal maps
   * @return the generated map, with every tile set
   */
  public TileMap generate(long seed) {
    Random rng = new Random(seed);

    if (!(TileDistribution.POINTS_OF_INTEREST.contains(TileType.SPAWN))) {
      throw new IllegalStateException("TileType.SPAWN is not listed as a point of interest.");
    }

    // Keep generating maps until one is found which is passable. Doing this inside this loop, with
    // the same rng, means that seeds are deterministic; even if a given seed isn't valid on the
    // first go, it will be used again to try and find another layout.
    while (true) {
      TileMap map = new TileMap(MAP_SIZE, seed);

      // Fill each block with one sampled terrain type.
      for (int blockX = 0; blockX < BLOCKS_PER_SIDE; blockX++) {
        for (int blockY = 0; blockY < BLOCKS_PER_SIDE; blockY++) {
          int blockStartX = blockX * (BLOCK_SIZE / TILE_SIZE);
          int blockStartY = blockY * (BLOCK_SIZE / TILE_SIZE);
          boolean nearWater = map.nextToTileOfGivenType(blockStartX, blockStartY, TileType.WATER);
          TileType type = nearWater
              ? distribution.boostedSample(rng, TileType.WATER, 2.00f)
              : distribution.sample(rng);

          for (int dx = 0; dx < BLOCK_SIZE / TILE_SIZE; dx++) {
            for (int dy = 0; dy < BLOCK_SIZE / TILE_SIZE; dy++) {
              map.setTileAtSquare(blockStartX + dx, blockStartY + dy, type);
            }
          }
        }
      }

      // Place POIs after the terrain so their grass patches are preserved. The +1 offset ensures
      // the 3x3 patch around each POI fits wholly within the map. Keep patches apart so placing one
      // POI cannot overwrite another.
      for (TileType poi : TileDistribution.POINTS_OF_INTEREST) {
        int x;
        int y;
        do {
          x = 1 + rng.nextInt((MAP_SIZE / TILE_SIZE) - 2);
          y = 1 + rng.nextInt((MAP_SIZE / TILE_SIZE) - 2);
        } while (!canPlacePoiAt(map, x, y));

        for (int dx = -1; dx < 2; dx++) {
          for (int dy = -1; dy < 2; dy++) {
            map.setTileAtSquare(x + dx, y + dy, TileType.GRASS);
          }
        }
        map.setTileAtSquare(x, y, poi);
      }

      // Validate that a clear path exists between POIs
      if (isMapPassable(map)) {
        return map;
      } else {
        continue;
      }
    }
  }

  private boolean canPlacePoiAt(TileMap map, int x, int y) {
    for (TilePosition existingPoi : map.getAllPointsOfInterest().values()) {
      if (Math.abs(existingPoi.x() - x) <= 2 && Math.abs(existingPoi.y() - y) <= 2) {
        return false;
      }
    }
    return true;
  }

  /**
   * Checks that every given point of interest can be reached from the first one by walking over
   * tiles the player can pass through, moving only up, down, left and right.
   *
   * <p>This is a breadth-first flood fill starting from the first point. Because walking is
   * symmetric, if every point is reachable from the first, then all points are connected to each
   * other.
   *
   * @param map the map to search
   * @return {@code true} if every point of interest is reachable from the first
   * @throws IllegalStateException if a map with zero POIs is given, or if a given POI is not
   *     passable itself.
   */
  public boolean isMapPassable(TileMap map) {
    Set<TilePosition> pointsOfInterest = Set.copyOf(map.getAllPointsOfInterest().values());
    if (pointsOfInterest.size() < 1) {
      throw new IllegalStateException("A map was given with no points of interest");
    } else if (pointsOfInterest.size() == 1) {
      return true;
    }

    for (TilePosition poi : pointsOfInterest) {
      if (map.isSquareSolidForPlayer(poi.x(), poi.y())) {
        throw new IllegalStateException("Map tile (" + poi.x() + ", " + poi.y() + ") is listed"
            + " as a POI, yet is itself not passable by players.");
      }
    }

    int size = map.getMapSizeInSquares();
    boolean[][] visited = new boolean[size][size];

    // Create a set of unvisited POIs; select the starting point
    Set<TilePosition> remainingPois = new HashSet<>(pointsOfInterest);
    TilePosition startPoi = remainingPois.stream()
        .findAny()
        .orElseThrow(() -> new IllegalStateException("No pointsOfInterest listed, despite checking"
            + " that at least one was present earlier" // Impossible to reach in production
        ));
    remainingPois.remove(startPoi);

    // Begin from the startPoi
    ArrayDeque<TilePosition> queue = new ArrayDeque<>();
    visited[startPoi.x()][startPoi.y()] = true;
    queue.add(startPoi);

    // Flood fill algorithm
    while (queue.size() != 0) {
      TilePosition currentNode = queue.poll(); // Select node at the head of the queue
      remainingPois.remove(currentNode); // This is a no-op if currentNode isn't in remainingPois
      if (remainingPois.size() == 0) {
        return true; // All POIs have been visited; the map is therefore passable
      }

      int[][] offsets = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
      for (int[] offset : offsets) {
        int dx = currentNode.x() + offset[0];
        int dy = currentNode.y() + offset[1];

        // If target square is out of bounds, skip it
        if (dx < 0 || dy < 0 || dx >= size || dy >= size) {
          continue;
        }
        // If the target square hasnt yet been visited, and is passable by players, the mark it as
        // visited, and add it to the queue
        else if (!visited[dx][dy] && !map.isSquareSolidForPlayer(dx, dy)) {
          visited[dx][dy] = true;
          queue.add(new TilePosition(dx, dy));
        }
      }
    }

    // If this point is reached, then all reachable squares have been exhaused, but there still
    // remains some POIs to visit.
    if (remainingPois.size() == 0) {
      throw new IllegalStateException("Algorithm has managed to terminate, despite all POIs having"
          + " been reached."
      );
    }

    return false;
  }
}
