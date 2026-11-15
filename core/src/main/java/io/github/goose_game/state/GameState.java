package io.github.goose_game.state;

/** Represents the state of the game that the user is in. */
public enum GameState {

  /** The player is currently browsing the main menu. */
  MAIN_MENU,

  /** The player is within the credits menu. */
  CREDITS,

  /** The map is currently being generated. */
  GENERATION,

  /** The player is in game. */
  GAME,

  /** The player is in game, and has triggered the tower defence event. */
  TOWER_DEFENCE,

  /** The player has paused the game. */
  PAUSED,

  /** The player has won the game. */
  VICTORY,

  /** The player has lost the game. */
  LOST
}
