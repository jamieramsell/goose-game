package io.github.goose_game.mapgen;

import java.util.ArrayDeque;
import java.util.Random;
import java.util.Set;

/**
 * Generates a random {@link TileMap} from a seed.
 *
 * <p>Generation is deterministic: the same seed always produces the same map, because all
 * randomness is drawn from a single {@link Random} created from that seed.
 *
 * <p>A map is built by reserving a spawn point and an escape point (each surrounded by a 3x3 patch
 * of grass), then filling every remaining tile by sampling a {@link TileDistribution}. Tiles next
 * to water have a boosted chance of being water, so that lakes form clusters.
 */
public class MapGenerator {

  /** The width and height of a single tile, in pixels. */
  public static final int TILE_SIZE = 256;

  /**
   * The width and height of the (square) map, in pixels. Must be a multiple of {@link #TILE_SIZE}.
   */
  public static final int MAP_SIZE = 4096;

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
      TileMap map = new TileMap(MAP_SIZE);
      
      // Create POIs first. Note that the +1, -2 values on the POI's x and y coords ensure that the
      // 3x3 patch surround it fits wholly within the map
      for (TileType poi : TileDistribution.POINTS_OF_INTEREST) {
        int x = 1 + rng.nextInt((MAP_SIZE / TILE_SIZE) - 2); 
        int y = 1 + rng.nextInt((MAP_SIZE / TILE_SIZE) - 2);

        for (int dx = -1; dx < 2; dx++) {
          for (int dy = -1; dy < 2; dy++) {
            map.setTileAtSquare(x + dx, y + dy, TileType.GRASS);
          }
        }
        map.setTileAtSquare(x, y, poi);
      }

      // Generate the rest of the map
      for (int x = 0; x < MAP_SIZE / TILE_SIZE; x++) {
        for (int y = 0; y < MAP_SIZE / TILE_SIZE; y++) {
          if (map.getTileAtSquare(x, y) == null) {
            // Set the tile at the given square. If next to water, boost this tile's chance of being
            // a water tile by 200%
            boolean nearWater = map.nextToTileOfGivenType(x, y, TileType.WATER);
            TileType t = nearWater
                ? distribution.boostedSample(rng, TileType.WATER, 2.00f)
                : distribution.sample(rng);
            map.setTileAtSquare(x, y, t);
          }
        }
      }

      // Validate that a clear path exists between POIs
      if (isMapPassable(map)) {
        return map;
      } else {
        continue;
      }
    }
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
    Set<TilePosition> remainingPois = Set.copyOf(pointsOfInterest);
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