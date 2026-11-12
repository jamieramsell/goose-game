package io.github.goose_game.mapgen;

import java.util.Random;

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
    TileMap map = new TileMap(MAP_SIZE);
    Random rng = new Random(seed);

    // Create spawn point
    int spawnX = Math.round(MAP_SIZE / TILE_SIZE * rng.nextFloat());
    int spawnY = Math.round(MAP_SIZE / TILE_SIZE * rng.nextFloat());

    for (int x = -1; x < 2; x++) {
      for (int y = -1; y < 2; y++) {
        map.setTileAtSquare(spawnX + x, spawnY + y, TileType.GRASS);
      }
    }
    map.setTileAtSquare(spawnX, spawnY, TileType.SPAWN);

    // Create escape point
    int escapeX = Math.round(MAP_SIZE / TILE_SIZE * rng.nextFloat());
    int escapeY = Math.round(MAP_SIZE / TILE_SIZE * rng.nextFloat());

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

    // TODO: validate that a clear path exists between the spawn and escape points.

    return map;
  }
}
