package io.github.goose_game.mapgen;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
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
  public static final int TILE_SIZE = 16;

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

    // Keep generating maps until one is found which is passable. Doing this inside this loop, with
    // the same rng, means that seeds are deterministic; even if a given seed isn't valid on the
    // first go, it will be used again to try and find another layout.
    while (true) {
      TileMap map = new TileMap(MAP_SIZE);
      
      // Create spawn point. Note that the +1, -2 values ensure that the 3x3 patch fit wholly within
      // the map
      int spawnX = 1 + rng.nextInt((MAP_SIZE / TILE_SIZE) - 2); 
      int spawnY = 1 + rng.nextInt((MAP_SIZE / TILE_SIZE) - 2);

      for (int x = -1; x < 2; x++) {
        for (int y = -1; y < 2; y++) {
          map.setTileAtSquare(spawnX + x, spawnY + y, TileType.GRASS);
        }
      }
      map.setTileAtSquare(spawnX, spawnY, TileType.SPAWN);

      // Create escape point
      int escapeX = 1 + rng.nextInt((MAP_SIZE / TILE_SIZE) - 2);
      int escapeY = 1 + rng.nextInt((MAP_SIZE / TILE_SIZE) - 2);

      for (int x = -1; x < 2; x++) {
        for (int y = -1; y < 2; y++) {
          map.setTileAtSquare(escapeX + x, escapeY + y, TileType.GRASS);
        }
      }
      map.setTileAtSquare(escapeX, escapeY, TileType.ESCAPE);

      // Generate the rest of the map
      for (int x = 0; x < MAP_SIZE / TILE_SIZE; x++) {
        for (int y = 0; y < MAP_SIZE / TILE_SIZE; y++) {
          if (map.getTileAtSquare(x, y) == null) {
            // Set the tile at the given square. If next to water, boost this tile's chance of being
            // a water tile by 75%
            boolean nearWater = map.nextToTileOfGivenType(x, y, TileType.WATER);
            TileType t = nearWater
                ? distribution.boostedSample(rng, TileType.WATER, 0.75f)
                : distribution.sample(rng);
            map.setTileAtSquare(x, y, t);
          }
        }
      }

      // Validate that a clear path exists between the spawn and escape points.
      int[] spawnPoint = {spawnX, spawnY};
      int[] escapePoint = {escapeX, escapeY};
      int[][] pois = {spawnPoint, escapePoint};

      if (isMapPassable(map, pois)) {
        return map;
      } else {
        continue;
      }
    }
  }

  /** A pair of tile indices used as a set element. */
  private record IntTuple(int x, int y) {}

  /**
   * Checks that every given point of interest can be reached from the first one by walking over
   * tiles the player can pass through, moving only up, down, left and right.
   *
   * <p>This is a breadth-first flood fill starting from the first point. Because walking is
   * symmetric, if every point is reachable from the first, then all points are connected to each
   * other.
   *
   * @param map the map to search
   * @param pointsOfInterest the points to connect, each given as a two-element array of {@code {x,
   *     y}} tile indices, such as the spawn point and the escape point
   * @return {@code true} if every point of interest is reachable from the first
   * @throws IllegalArgumentException if fewer than two points are given, or if a given POI is not
   *     passable itself.
   */
  public boolean isMapPassable(TileMap map, int[][] pointsOfInterest) {
    if (pointsOfInterest.length < 2) {
      throw new IllegalArgumentException("pointsOfInterest must contain at least two coordinates");
    }

    for (int[] poi : pointsOfInterest) {
      if (map.isSquareSolidForPlayer(poi[0], poi[1])) {
        throw new IllegalArgumentException("Map tile (" + poi[0] + ", " + poi[1] + ") is listed as"
            + " a POI, yet is itself not passable by players.");
      }
    }
    
    int size = map.getMapSizeInSquares();
    boolean[][] visited = new boolean[size][size];

    // Create a set of unvisited POIs; select the starting point
    TilePosition startPoi = new TilePosition(pointsOfInterest[0][0], pointsOfInterest[0][1]);
    Set<TilePosition> remainingPois = new HashSet<>(
        Arrays.stream(pointsOfInterest)
        .map(array -> new TilePosition(array[0], array[1]))
        .toList()
    ); 
    remainingPois.remove(startPoi); // Removed after conversion to a set to ensure no duplicates

    // Begin from the startPoi
    ArrayDeque<TilePosition> queue = new ArrayDeque<>();
    visited[startPoi.x][startPoi.y] = true;
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
        int dx = currentNode.x + offset[0];
        int dy = currentNode.y + offset[1];

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