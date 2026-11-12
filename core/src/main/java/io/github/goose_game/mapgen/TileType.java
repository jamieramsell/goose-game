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

  public final boolean playerPassable;
  public final boolean goosePassable;

  private TileType(boolean playerPassable, boolean goosePassable) {
    this.playerPassable = playerPassable;
    this.goosePassable = goosePassable;
  }
}
