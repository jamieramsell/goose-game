package io.github.goose_game.state;

import java.util.Random;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.utils.Disposable;
import io.github.goose_game.Main;
import io.github.goose_game.game.GameScreen;
import io.github.goose_game.mapgen.MapGenerator;
import io.github.goose_game.mapgen.TileMap;
import io.github.goose_game.menu.CreditsPage;
import io.github.goose_game.menu.GameLostPage;
import io.github.goose_game.menu.GameGenerationPage;
import io.github.goose_game.menu.MainMenu;
import io.github.goose_game.menu.PauseMenu;
import io.github.goose_game.menu.VictoryPage;

/**
 * Owns the game's current {@link GameState} and performs transitions between states.
 *
 * <p>Each transition method validates the move against {@link GameStateGraph}, updates the current
 * state, switches {@link Main}'s screen, and disposes the screen that was being left. Calling a
 * transition that the graph does not allow throws an {@link IllegalArgumentException} and leaves
 * the state and screen unchanged.
 *
 * <p>Creating a manager immediately puts the game in {@link GameState#MAIN_MENU} and shows the
 * {@link MainMenu}.
 */
public class GameStateManager {

  private GameState gameState;
  private final Main game;
  private GameScreen pausedGame = null;
  private boolean towerDefenceEventBeenTriggered = false;

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

    pausedGameGarbageCollector();
  }

  /**
   * Transitions to {@link GameState#GENERATION}, showing a {@link GameGenerationPage} while a new
   * map is generated.
   *
   * <p>This is the first step of starting a new game; the generation screen leads on to {@link
   * GameState#GAME}. Any paused game is discarded.
   *
   * @param currentScreen the screen being left; disposed after the new screen is set
   * @param seed the (nullable) seed to use to generate the map
   * @throws IllegalArgumentException if map generation cannot be started from the current state
   */
  public void startNewGame(Disposable currentScreen, String seed) {
    if (!GameStateGraph.canTransition(gameState, GameState.GENERATION)) {
      throw new IllegalArgumentException(
          "Illegal transition: GameState.GENERATION is not currently accessible");
    }

    // Screen & state management
    Screen loadingScreen = new GameGenerationPage(game);
    game.setScreen(loadingScreen);
    gameState = GameState.GENERATION;
    currentScreen.dispose();
    towerDefenceEventBeenTriggered = false;

    // Parse map seed
    long longSeed;
    if (seed == null || seed.isBlank()) {
      longSeed = new Random().nextLong();
    } else {
      longSeed = seed.hashCode();
    }
   
    // Don't use the GDX thread to generate the map
    Thread worker = new Thread(() -> {
      // Now generate the map
      MapGenerator generator = new MapGenerator();
      TileMap map = generator.generate(longSeed);

      // Return to GDX thread once map has been generated in order to draw it
      Gdx.app.postRunnable(() -> {
        // Finally, draw the map and update state once more
        game.setScreen(new GameScreen(game, this, map));
        gameState = GameState.GAME;
        loadingScreen.dispose();

        // Garbage collect (you never know lol)
        pausedGameGarbageCollector();
      });
    });
    
    worker.setDaemon(true); // Kill the worker thread if the window is quit mid session
    worker.start();
  }

  /**
   * Transitions to {@link GameState#GENERATION}, showing a {@link GameGenerationPage} while a new
   * map is generated, using a random seed.
   *
   * <p>This is the first step of starting a new game; the generation screen leads on to {@link
   * GameState#GAME}. Any paused game is discarded.
   *
   * @param currentScreen the screen being left; disposed after the new screen is set
   * @throws IllegalArgumentException if map generation cannot be started from the current state
   */
  public void startNewGame(Disposable currentScreen) {
    startNewGame(currentScreen, null);
  }

  /**
   * Transitions to {@link GameState#GAME}, either starting a new {@link GameScreen}, or resuming
   * from where the game left off.
   *
   * <p>If the game is paused, the paused {@link GameScreen} is reused and not disposed; otherwise a
   * new one is created.
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
        pausedGame = null;
        break;
      default:
        startNewGame(currentScreen, null);;
        break;
    }

    gameState = GameState.GAME;
    currentScreen.dispose();

    pausedGameGarbageCollector();
  }

  /**
   * Triggers the tower defence event, if one hasn't been triggered in this game already.
   *
   * @return whether or not the tower defence event was triggered
   * @throws IllegalArgumentException if the tower defence event cannot legally be triggered from
   *     the current state (note this is not thrown if in game and the event has simply already been
   *     triggered once)
   */
  public boolean towerDefenceEvent() {
    if (towerDefenceEventBeenTriggered) {
      return false;
    }

    if (!GameStateGraph.canTransition(gameState, GameState.TOWER_DEFENCE)) {
      throw new IllegalArgumentException(
          "Illegal transition: GameState.TOWER_DEFENCE is not currently accessible");
    }

    towerDefenceEventBeenTriggered = true;
    gameState = GameState.TOWER_DEFENCE;
    pausedGameGarbageCollector();
    return true;
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

    pausedGameGarbageCollector();
  }

  /**
   * Transitions to {@link GameState#PAUSED}, showing a {@link PauseMenu} over the running game.
   *
   * <p>The game screen is not disposed. It is kept so it can be drawn behind the pause menu and
   * resumed by {@link #play(Disposable)}, and is disposed if the player leaves the paused game
   * instead.
   *
   * @param gameScreen the running game screen to pause
   * @throws IllegalArgumentException if the game cannot be paused from the current state
   */
  public void pause(GameScreen gameScreen) {
    if (!GameStateGraph.canTransition(gameState, GameState.PAUSED)) {
      throw new IllegalArgumentException(
          "Illegal transition: GameState.PAUSED is not currently" + " accessible");
    }
    pausedGameGarbageCollector();

    gameState = GameState.PAUSED;
    pausedGame = gameScreen;
    game.setScreen(new PauseMenu(game, this, gameScreen));
  }

  /**
   * Transitions to {@link GameState#VICTORY}, showing a {@link VictoryPage} over the running game.
   *
   * <p>The game screen is not disposed. It is kept so it can be drawn behind the victory menu,
   * instead being disposed when the player leaves the game.
   *
   * @param gameScreen the running game screen
   * @throws IllegalArgumentException if the game cannot be won from the current state
   */
  public void victory(GameScreen gameScreen) {
    if (!GameStateGraph.canTransition(gameState, GameState.VICTORY)) {
      throw new IllegalArgumentException(
          "Illegal transition: GameState.VICTORY is not currently accessible");
    }

    pausedGameGarbageCollector();
    
    gameState = GameState.VICTORY;
    pausedGame = gameScreen;
    game.setScreen(new VictoryPage(game, this, gameScreen));
  }

  /**
   * Transitions to {@link GameState#LOST}, showing a {@link GameLostPage} over the running game.
   *
   * <p>The game screen is not disposed. It is kept so it can be drawn behind the 'game over' menu,
   * instead being disposed when the player leaves the game.
   *
   * @param gameScreen the running game screen
   * @throws IllegalArgumentException if the game cannot be lost from the current state
   */
  public void lose(GameScreen gameScreen) {
    if (!GameStateGraph.canTransition(gameState, GameState.LOST)) {
      throw new IllegalArgumentException(
          "Illegal transition: GameState.LOST is not currently accessible");
    }

    pausedGameGarbageCollector();
    
    gameState = GameState.LOST;
    pausedGame = gameScreen;
    game.setScreen(new GameLostPage(game, this, gameScreen));
  }

  /**
   * @return the current {@link GameState} that the game is in.
   */
  public GameState getGameState() {
    return gameState;
  }

  /** Disposes and deletes the paused game record, if required. Called by every state change. */
  private void pausedGameGarbageCollector() {
    if (pausedGame != null) {
      pausedGame.dispose();
      pausedGame = null;
    }
  }
}
