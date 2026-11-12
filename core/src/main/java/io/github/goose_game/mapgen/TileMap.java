package io.github.goose_game.mapgen;

/**
 * A square grid of {@link TileType}s making up the game world.
 *
 * <p>Tiles can be addressed in two coordinate systems: pixels (world coordinates, used by gameplay
 * code) and squares (tile indices, used during generation). One square is {@link
 * MapGenerator#TILE_SIZE} pixels wide.
 */
public class TileMap {

  private final TileType[][] map;

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
  }

  /**
   * Returns the tile containing the given world position.
   *
   * @param x the x position, in pixels
   * @param y the y position, in pixels
   * @return the tile at that position, or {@code null} if it has not been set
   */
  public TileType getTileAtPixel(int x, int y) {
    x = mapPixelToSquare(x);
    y = mapPixelToSquare(y);
    return map[x][y];
  }

  /**
   * Returns the tile at the given tile index.
   *
   * @param tileX the x index, in tiles
   * @param tileY the y index, in tiles
   * @return the tile at that index, or {@code null} if it has not been set
   */
  TileType getTileAtSquare(int tileX, int tileY) {
    return map[tileX][tileY];
  }

  /**
   * Returns whether the player is blocked by the tile at the given world position.
   *
   * @param x the x position, in pixels
   * @param y the y position, in pixels
   * @return {@code true} if the player cannot pass through that tile
   */
  public boolean isPixelSolidForPlayer(int x, int y) {
    x = mapPixelToSquare(x);
    y = mapPixelToSquare(y);
    return !map[x][y].playerPassable;
  }

  /**
   * Returns whether geese are blocked by the tile at the given world position. Geese can swim, so
   * water is not solid for them.
   *
   * @param x the x position, in pixels
   * @param y the y position, in pixels
   * @return {@code true} if geese cannot pass through that tile
   */
  public boolean isPixelSolidForGeese(int x, int y) {
    x = mapPixelToSquare(x);
    y = mapPixelToSquare(y);
    return !map[x][y].goosePassable;
  }

  int mapPixelToSquare(int pixel) {
    return pixel / MapGenerator.TILE_SIZE;
  }

  /**
   * Sets the tile at the given tile index.
   *
   * @param tileX the x index, in tiles
   * @param tileY the y index, in tiles
   * @param tile the tile type to place
   */
  void setTileAtSquare(int tileX, int tileY, TileType tile) {
    map[tileX][tileY] = tile;
  }

  /**
   * Returns whether any of the four orthogonal neighbours of the given tile is of the given type.
   *
   * @param tileX the x index of the tile, in tiles
   * @param tileY the y index of the tile, in tiles
   * @param tile the tile type to look for
   * @return {@code true} if an orthogonal neighbour is of that type
   */
  boolean nextToTileOfGivenType(int tileX, int tileY, TileType tile) {
    int[][] offsets = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
    int size = getMapSizeInSquares();

    for (int[] offset : offsets) {
      int dx = tileX + offset[0];
      int dy = tileY + offset[1];

      if (dx < 0 || dy < 0 || dx > size || dy > size) {
        continue; // If current square is out of bounds, skip it
      } else if (getTileAtSquare(tileX + dx, tileY + dy).equals(tile)) {
        return true;
      }
    }

    return false;
  }

  /** @return the map size in terms of the number of squares in each plane. */
  int getMapSizeInSquares() {
    return map.length;
  }
}
