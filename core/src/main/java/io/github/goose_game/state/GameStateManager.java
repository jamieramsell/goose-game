package io.github.goose_game.state;

import com.badlogic.gdx.utils.Disposable;
import io.github.goose_game.Main;
import io.github.goose_game.game.GameScreen;
import io.github.goose_game.menu.CreditsPage;
import io.github.goose_game.menu.MainMenu;
import io.github.goose_game.menu.PauseMenu;

/**
 * Owns the game's current {@link GameState} and performs transitions between states.
 *
 * <p>Each transition method validates the move against {@link GameStateGraph}, updates the current
 * state, switches {@link Main}'s screen to a newly created one, and disposes the screen that was
 * being left. Calling a transition that the graph does not allow throws an {@link
 * IllegalArgumentException} and leaves the state and screen unchanged.
 *
 * <p>Creating a manager immediately puts the game in {@link GameState#MAIN_MENU} and shows the
 * {@link MainMenu}.
 */
public class GameStateManager {

  private GameState gameState;
  private final Main game;
  private GameScreen pausedGame = null;

  /**
   * Creates the manager, enters {@link GameState#MAIN_MENU} and shows the main menu.
   *
   * <p>Must be called after libGDX has started, as the menu loads textures.
   *
   * @param game the game whose screens this manager switches between
   */
  public GameStateManager(Main game) {
    this.game = game;

    gameState = GameState.MAIN_MENU;
    game.setScreen(new MainMenu(game, this));
  }

  /**
   * Transitions to {@link GameState#MAIN_MENU}, showing a new {@link MainMenu}.
   *
   * @param currentScreen the screen being left; disposed after the new screen is set
   * @throws IllegalArgumentException if the main menu cannot be reached from the current state
   */
  public void mainMenu(Disposable currentScreen) {
    if (!GameStateGraph.canTransition(gameState, GameState.MAIN_MENU)) {
      throw new IllegalArgumentException(
          "Illegal transition: GameState.MAIN_MENU is not currently" + " accessible");
    }

    gameState = GameState.MAIN_MENU;
    game.setScreen(new MainMenu(game, this));
    currentScreen.dispose();

    if (pausedGame != null) {
      pausedGame.dispose();
    }
  }

  /**
   * Transitions to {@link GameState#GAME}, either starting a new {@link GameScreen}, or resuming
   * from where the game left off.
   *
   * @param currentScreen the screen being left; disposed after the new screen is set
   * @throws IllegalArgumentException if the game cannot be started from the current state
   */
  public void play(Disposable currentScreen) {
    if (!GameStateGraph.canTransition(gameState, GameState.GAME)) {
      throw new IllegalArgumentException(
          "Illegal transition: GameState.GAME is not currently" + " accessible");
    }

    switch (gameState) { // Resume existing game if the game is currently paused
      case PAUSED:
        game.setScreen(pausedGame);
        break;
      default:
        game.setScreen(new GameScreen(game, this));
    }

    gameState = GameState.GAME;
    currentScreen.dispose();
  }

  /**
   * Transitions to {@link GameState#CREDITS}, showing a new {@link CreditsPage}.
   *
   * @param currentScreen the screen being left; disposed after the new screen is set
   * @throws IllegalArgumentException if the credits cannot be reached from the current state
   */
  public void credits(Disposable currentScreen) {
    if (!GameStateGraph.canTransition(gameState, GameState.CREDITS)) {
      throw new IllegalArgumentException(
          "Illegal transition: GameState.CREDITS is not currently" + " accessible");
    }

    gameState = GameState.CREDITS;
    game.setScreen(new CreditsPage(game, this));
    currentScreen.dispose();

    if (pausedGame != null) {
      pausedGame.dispose();
    }
  }

  public void pause(GameScreen gameScreen) {
    if (!GameStateGraph.canTransition(gameState, GameState.PAUSED)) {
      throw new IllegalArgumentException(
          "Illegal transition: GameState.PAUSED is not currently" + " accessible");
    }
    gameState = GameState.PAUSED;
    pausedGame = gameScreen;
    game.setScreen(new PauseMenu(game, this, gameScreen));
  }

  public GameState getGameState() {
    return gameState;
  }
}
