package io.github.goose_game.mapgen;

import java.util.HashSet;
import java.util.Set;

/**
 * A square grid of {@link TileType}s making up the game world.
 *
 * <p>Tiles can be addressed in two coordinate systems: pixels (world coordinates, used by gameplay
 * code) and squares (tile indices, used during generation). One square is {@link
 * MapGenerator#TILE_SIZE} pixels wide.
 */
public class TileMap {

  private final TileType[][] map;
  private final Set<TilePosition> pointsOfInterest;

  /**
   * Creates an empty map, where every tile is {@code null} until set.
   *
   * @param mapSize the width and height of the map, in pixels
   * @throws IllegalArgumentException if {@code mapSize} is not positive, or is not a multiple of
   *     {@link MapGenerator#TILE_SIZE}
   */
  public TileMap(int mapSize) {
    if (mapSize <= 0) {
      throw new IllegalArgumentException("mapSize must be greater than 0");
    } else if (mapSize % MapGenerator.TILE_SIZE != 0) {
      throw new IllegalArgumentException("mapSize must be a multiple of MapGenerator.TILE_SIZE");
    }

    int mapSizeInTiles = mapPixelToSquare(mapSize);
    map = new TileType[mapSizeInTiles][mapSizeInTiles];
    pointsOfInterest = new HashSet<>();
  }

  /**
   * Returns the tile containing the given world position.
   *
   * @param x the x position, in pixels
   * @param y the y position, in pixels
   * @return the tile at that position, or {@code null} if it has not been set
   * @throws IllegalArgumentException if the position is outside the map
   */
  public TileType getTileAtPixel(int x, int y) {
    x = mapPixelToSquare(x);
    y = mapPixelToSquare(y);
    checkCoordinatesAreInBounds(x, y);
    return map[x][y];
  }

  /**
   * Returns the tile at the given tile index.
   *
   * @param tileX the x index, in tiles
   * @param tileY the y index, in tiles
   * @return the tile at that index, or {@code null} if it has not been set
   * @throws IllegalArgumentException if the index is outside the map
   */
  TileType getTileAtSquare(int tileX, int tileY) {
    checkCoordinatesAreInBounds(tileX, tileY);
    return map[tileX][tileY];
  }

  /**
   * Returns whether the player is blocked by the tile at the given world position.
   *
   * @param x the x position, in pixels
   * @param y the y position, in pixels
   * @return {@code true} if the player cannot pass through that tile
   * @throws IllegalArgumentException if the position is outside the map
   */
  public boolean isPixelSolidForPlayer(int x, int y) {
    x = mapPixelToSquare(x);
    y = mapPixelToSquare(y);
    checkCoordinatesAreInBounds(x, y);
    return !map[x][y].playerPassable;
  }

  /**
   * Returns whether the player is blocked by the tile at the given tile index.
   *
   * @param x the x index, in tiles
   * @param y the y index, in tiles
   * @return {@code true} if the player cannot pass through that tile
   * @throws IllegalArgumentException if the index is outside the map
   */
  boolean isSquareSolidForPlayer(int x, int y) {
    checkCoordinatesAreInBounds(x, y);
    return !map[x][y].playerPassable;
  }

  /**
   * Returns whether geese are blocked by the tile at the given world position. Geese can swim, so
   * water is not solid for them.
   *
   * @param x the x position, in pixels
   * @param y the y position, in pixels
   * @return {@code true} if geese cannot pass through that tile
   * @throws IllegalArgumentException if the position is outside the map
   */
  public boolean isPixelSolidForGeese(int x, int y) {
    x = mapPixelToSquare(x);
    y = mapPixelToSquare(y);
    checkCoordinatesAreInBounds(x, y);
    return !map[x][y].goosePassable;
  }

  /**
   * Returns whether geese are blocked by the tile at the given tile index. Geese can swim, so water
   * is not solid for them.
   *
   * @param x the x index, in tiles
   * @param y the y index, in tiles
   * @return {@code true} if geese cannot pass through that tile
   * @throws IllegalArgumentException if the index is outside the map
   */
  boolean isSquareSolidForGeese(int x, int y) {
    checkCoordinatesAreInBounds(x, y);
    return !map[x][y].goosePassable;
  }

  /**
   * Converts a position in pixels to a tile index.
   *
   * @param pixel the position, in pixels
   * @return the index of the tile containing that position
   */
  int mapPixelToSquare(int pixel) {
    return pixel / MapGenerator.TILE_SIZE;
  }

  /**
   * Sets the tile at the given tile index.
   *
   * @param tileX the x index, in tiles
   * @param tileY the y index, in tiles
   * @param tile the tile type to place
   * @throws IllegalArgumentException if the index is outside the map
   */
  void setTileAtSquare(int tileX, int tileY, TileType tile) {
    checkCoordinatesAreInBounds(tileX, tileY);

    if (TileDistribution.POINTS_OF_INTEREST.contains(map[tileX][tileY])) {
      pointsOfInterest.remove(new TilePosition(tileX, tileY));
    }

    map[tileX][tileY] = tile;

    if (TileDistribution.POINTS_OF_INTEREST.contains(tile)) {
      pointsOfInterest.add(new TilePosition(tileX, tileY));
    }
  }

  /**
   * Returns whether any of the four orthogonal neighbours of the given tile is of the given type.
   *
   * @param tileX the x index of the tile, in tiles
   * @param tileY the y index of the tile, in tiles
   * @param tile the tile type to look for
   * @return {@code true} if an orthogonal neighbour is of that type
   * @throws IllegalArgumentException if the given tile is outside the map; neighbours that fall
   *     outside the map are ignored
   */
  boolean nextToTileOfGivenType(int tileX, int tileY, TileType tile) {
    checkCoordinatesAreInBounds(tileX, tileY);
    int[][] offsets = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
    int size = getMapSizeInSquares();

    for (int[] offset : offsets) {
      int dx = tileX + offset[0];
      int dy = tileY + offset[1];

      if (dx < 0 || dy < 0 || dx >= size || dy >= size) {
        continue; // If current square is out of bounds, skip it
      } else if (getTileAtSquare(dx, dy) == tile) {
        return true;
      }
    }

    return false;
  }

  /** @return the map size in terms of the number of squares in each plane. */
  int getMapSizeInSquares() {
    return map.length;
  }

  /**
   * Checks that the given tile index lies within the map.
   *
   * @param x the x index, in tiles
   * @param y the y index, in tiles
   * @throws IllegalArgumentException if either index is negative or is not less than the map size
   */
  private void checkCoordinatesAreInBounds(int x, int y) {
    if (x < 0 || y < 0 || x >= map.length || y >= map.length) {
      throw new IllegalArgumentException("The given coordinates are out of bounds.");
    }
  }

  /** @return all tiles on the map which are marked as points of interest. */
  Set<TilePosition> getAllPointsOfInterest() {
    return pointsOfInterest;
  }
}
