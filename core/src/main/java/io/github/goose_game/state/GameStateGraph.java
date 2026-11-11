package io.github.goose_game.state;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * The set of game states, and the transitions between them, are represented by a digraph, whose
 * nodes are each of the GameStates, and the edges represent each valid transition between two
 * states.
 */
public class GameStateGraph {

  private static final Map<GameState, Set<GameState>> TRANSITIONS = new EnumMap<>(GameState.class);

  // Populate the graph
  static {
    TRANSITIONS.put(GameState.MAIN_MENU, EnumSet.of(GameState.GAME, GameState.CREDITS));
    TRANSITIONS.put(GameState.CREDITS, EnumSet.of(GameState.MAIN_MENU));
    TRANSITIONS.put(
        GameState.GAME, EnumSet.of(GameState.PAUSED, GameState.VICTORY, GameState.LOST));
    TRANSITIONS.put(GameState.PAUSED, EnumSet.of(GameState.GAME, GameState.MAIN_MENU));
    TRANSITIONS.put(GameState.VICTORY, EnumSet.of(GameState.MAIN_MENU));
    TRANSITIONS.put(GameState.LOST, EnumSet.of(GameState.MAIN_MENU));
  }

  /**
   * Determines whether or not the given transition is valid.
   *
   * @param from the current {@link GameState}
   * @param to the {@link GameState} to transition to
   */
  public static boolean canTransition(GameState from, GameState to) {
    return TRANSITIONS.get(from).contains(to);
  }
}
