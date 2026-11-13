package io.github.goose_game.mapgen;

/** Represents the different types of tile on the map. */
public enum TileType {

  /** The player spawn point. */
  SPAWN(true, true),

  /** The player's escape point. */
  ESCAPE(true, true),

  /** A plain, grassy tile. Passable by all. */
  GRASS(true, true),

  /** A water tile, which geese can swim across, but players cannot. */
  WATER(false, true),

  /** An impassible tile occupied by a university building. */
  BUILDING(false, false),

  /** An impassible tile occupied by vegetation. */
  BUSH(false, false);

  /** Whether the player can walk over this tile. */
  public final boolean playerPassable;

  /** Whether geese can move over this tile. */
  public final boolean goosePassable;

  /**
   * Creates a tile type.
   *
   * @param playerPassable whether the player can walk over this tile
   * @param goosePassable whether geese can move over this tile
   */
  private TileType(boolean playerPassable, boolean goosePassable) {
    this.playerPassable = playerPassable;
    this.goosePassable = goosePassable;
  }
}
