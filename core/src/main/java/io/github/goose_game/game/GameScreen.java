package io.github.goose_game.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import io.github.goose_game.Main;
import io.github.goose_game.state.GameState;
import io.github.goose_game.state.GameStateManager;
import io.github.goose_game.utils.Node;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Main gameplay screen, which owns the scene graph (player, enemies, weapons), the world camera,
 * and the HUD (timer, score, attack warning, health bar), and drives the 5 minute survival round.
 *
 * <p>The round is won by surviving until the timer reaches zero, or lost if the player's health
 * reaches zero. Halfway through the round a large enemy spawns. Win, lose and pause transitions are
 * delegated to the {@link GameStateManager}.
 */
public class GameScreen extends ScreenAdapter {

  // Gameplay values
  private static final float WORLD_WIDTH = 320f;
  private static final float WORLD_HEIGHT = 320f;
  private static final float GAME_DURATION_SECONDS = 5 * 60;
  private static final float ATTACK_MESSAGE_DURATION_SECONDS = 3f;

  // Framework
  private final Main game;
  private final GameStateManager stateManager;

  private final Batch batch;
  private final TextureAtlas atlas;
  private final Viewport viewport = new ExtendViewport(WORLD_WIDTH, WORLD_HEIGHT);
  private final OrthographicCamera camera = new OrthographicCamera();

  // HUD
  private final Stage hudStage = new Stage(new ScreenViewport());
  private final BitmapFont timerFont = new BitmapFont();
  private final BitmapFont attackFont = new BitmapFont();
  private final BitmapFont scoreFont = new BitmapFont();
  private final BitmapFont scorePopupFont = new BitmapFont();

  private final Label timerLabel;
  private final Label scoreLabel;
  private final Label attackLabel;
  private final ArrayList<ScorePopup> scorePopups = new ArrayList<>();

  // Scene & Rendering
  private final Node root = new Node(0, 0, 0, 0);
  private final EnemyManager enemyManager;
  private Texture background = new Texture(Gdx.files.internal("floor_test.png"));
  private Sprite playerTexture;
  private final Animation<Sprite> stickAttackTexture;
  Node player;

  // Game state
  private boolean largeEnemySpawned;
  private float remainingTime = GAME_DURATION_SECONDS;
  private float attackMessageTime;
  private float scoreRemainder;
  private int score;
  private int displayedSeconds = -1;

  // Debug
  private boolean showEntityBounds;
  private final ShapeRenderer debugShapeRenderer = new ShapeRenderer();

  /**
   * Creates the game screen, builds the HUD, and sets up the player, enemy manager and weapon.
   *
   * @param game application instance providing the shared sprite batch
   * @param stateManager the current {@link GameStateManager} instance
   */
  public GameScreen(Main game, GameStateManager stateManager) {
    this.game = game;
    this.stateManager = stateManager;
    this.batch = game.getBatch();
    this.atlas = new TextureAtlas(Gdx.files.internal("spritesheet.atlas"));

    timerFont.getData().setScale(4f);
    attackFont.getData().setScale(3f);
    scoreFont.getData().setScale(2.625f);
    scorePopupFont.getData().setScale(0.75f);
    timerLabel = new Label("", new Label.LabelStyle(timerFont, Color.WHITE));
    scoreLabel = new Label("Score: 0", new Label.LabelStyle(scoreFont, Color.WHITE));
    attackLabel = new Label("Large Goose Attack!", new Label.LabelStyle(attackFont, Color.WHITE));

    Table timerTable = new Table();
    timerTable.setFillParent(true);
    timerTable.top();
    timerTable.add(timerLabel).padTop(12);
    hudStage.addActor(timerTable);

    Table scoreTable = new Table();
    scoreTable.setFillParent(true);
    scoreTable.top().left();
    scoreTable.add(scoreLabel).padTop(12).padLeft(12);
    hudStage.addActor(scoreTable);

    Table attackTable = new Table();
    attackTable.setFillParent(true);
    attackTable.center();
    attackLabel.setVisible(false);
    attackTable.add(attackLabel);
    hudStage.addActor(attackTable);

    for (Texture texture : atlas.getTextures()) {
      texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
    }

    playerTexture = atlas.createSprite("player_test");

    this.stickAttackTexture =
        new Animation<>(1, atlas.createSprites("sheet_6_katana_slash"), Animation.PlayMode.NORMAL);

    this.root.add_child("player", new Player(8, 6, 16, 20, atlas, 64, 200, camera));

    enemyManager =
        new EnemyManager(atlas, (Player) this.root.get_child("player"), this::onEnemyDispersed);
    this.root.add_child("enemyManager", enemyManager);

    this.root
        .get_child("player")
        .add_child(
            "weapon_test", new Weapon(0, 0, 32, 32, stickAttackTexture, atlas, 1, 20, 5, 120));

    viewport.setCamera(camera);

    background.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
  }

  @Override
  public void resize(int width, int height) {
    viewport.update(width, height, false);
    hudStage.getViewport().update(width, height, true);
  }

  @Override
  public void render(float delta) {
    ScreenUtils.clear(0, 0, 0.1f, 1);
    update(delta);

    viewport.apply();
    batch.setProjectionMatrix(viewport.getCamera().combined);
    batch.begin();

    batch.draw(background, -32 * 64, -32 * 64, 0, 0, 32 * 128, 32 * 128);
    root.draw(batch);
    drawScorePopups();

    batch.end();

    if (showEntityBounds) {
      debugShapeRenderer.setProjectionMatrix(camera.combined);
      debugShapeRenderer.begin(ShapeRenderer.ShapeType.Line);
      debugShapeRenderer.setColor(Color.GREEN);
      root.drawDebugBounds(debugShapeRenderer);
      debugShapeRenderer.end();
    }

    drawPlayerHealthBar();

    hudStage.act(delta);
    hudStage.draw();
  }

  /** Draws a health bar beneath the player in world space. */
  private void drawPlayerHealthBar() {
    Player player = (Player) root.get_child("player");
    float width = 24f;
    float height = 3f;
    float x = player.position.x - width / 2;
    float y = player.position.y - 14f;

    debugShapeRenderer.setProjectionMatrix(camera.combined);
    debugShapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
    debugShapeRenderer.setColor(Color.DARK_GRAY);
    debugShapeRenderer.rect(x, y, width, height);
    debugShapeRenderer.setColor(Color.GREEN);
    debugShapeRenderer.rect(x, y, width * player.getHealth() / player.getMaxHealth(), height);
    debugShapeRenderer.end();
  }

  /** Draws score popups, flashing them on and off while they are active. */
  private void drawScorePopups() {
    for (ScorePopup popup : scorePopups) {
      float elapsed = 0.5f - popup.timeRemaining;
      if (((int) (elapsed / 0.1f)) % 2 == 0) {
        scorePopupFont.draw(batch, popup.text, popup.x, popup.y + 18);
      }
    }
  }

  /**
   * Awards the enemy's dispersal score and spawns a score popup at its position.
   *
   * @param enemy the enemy that was dispersed
   */
  private void onEnemyDispersed(Enemy enemy) {
    int points = enemy.getDispersalScore();
    score += points;
    updateScoreLabel();
    scorePopups.add(new ScorePopup("+" + points, enemy.position.x, enemy.position.y));
  }

  /**
   * Awards one point per second survived.
   *
   * @param delta time elapsed since the last frame, in seconds
   */
  private void updateScore(float delta) {
    scoreRemainder += delta;
    int earnedPoints = (int) scoreRemainder;
    if (earnedPoints > 0) {
      score += earnedPoints;
      scoreRemainder -= earnedPoints;
      updateScoreLabel();
    }
  }

  /** Refreshes the score label text. */
  private void updateScoreLabel() {
    scoreLabel.setText("Score: " + score);
  }

  /**
   * Ticks down popup lifetimes and removes expired ones.
   *
   * @param delta time elapsed since the last frame, in seconds
   */
  private void updateScorePopups(float delta) {
    Iterator<ScorePopup> iterator = scorePopups.iterator();
    while (iterator.hasNext()) {
      ScorePopup popup = iterator.next();
      popup.timeRemaining -= delta;
      if (popup.timeRemaining <= 0) {
        iterator.remove();
      }
    }
  }

  /**
   * Advances the game by one frame
   * 
   * <p>Handles the debug toggle and pause key, updates the timer, score, the large enemy event,
   * scene nodes, win/lose checks and the camera.
   *
   * @param delta time elapsed since the last frame, in seconds
   */
  private void update(float delta) {
    if (Gdx.input.isKeyJustPressed(Input.Keys.F1)) {
      showEntityBounds = !showEntityBounds;
    }

    // Updates which should only take place while game is in action should be placed within this
    // if statement:
    if (stateManager.getGameState() == GameState.GAME) {
      if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
        stateManager.pause(this);
      }

      if (stateManager.getGameState() == GameState.GAME) {
        updateScore(Math.min(delta, remainingTime));
        updateScorePopups(delta);
        remainingTime = Math.max(0, remainingTime - delta);
        updateTimerLabel();
        updateAttackMessage(delta);

        if (remainingTime <= GAME_DURATION_SECONDS / 2 && !largeEnemySpawned) {
          largeEnemySpawned = true;
          enemyManager.spawnLargeEnemy();
          attackMessageTime = ATTACK_MESSAGE_DURATION_SECONDS;
          attackLabel.setVisible(true);
        }

        if (remainingTime == 0) { // Time has reached 0, you've escaped!
          stateManager.victory(this);
        } else {
          /* Only update nodes while game is active */
          root.update(delta);
          Player player = (Player) root.get_child("player");

          if (player.getHealth() <= 0) { // Player dead, lose condition
            stateManager.lose(this);
          } else {
            camera.position.lerp(new Vector3(player.position.x, player.position.y, 0), 0.02f);
          }
        }
      }
    }
  }

  /**
   * Counts down the attack warning and hides it when expired.
   *
   * @param delta time elapsed since the last frame, in seconds
   */
  private void updateAttackMessage(float delta) {
    if (attackMessageTime > 0) {
      attackMessageTime = Math.max(0, attackMessageTime - delta);
      attackLabel.setVisible(attackMessageTime > 0);
    }
  }

  /** Updates the timer label. */
  private void updateTimerLabel() {
    int seconds = (int) Math.ceil(remainingTime);
    if (seconds != displayedSeconds) {
      displayedSeconds = seconds;
      timerLabel.setText(String.format("%02d:%02d", seconds / 60, seconds % 60));
    }
  }

  @Override
  public void dispose() {
    debugShapeRenderer.dispose();
    hudStage.dispose();
    timerFont.dispose();
    attackFont.dispose();
    scoreFont.dispose();
    scorePopupFont.dispose();
  }

  /** Text showing points earned at a world position. */
  private static class ScorePopup {
    private final String text;
    private final float x;
    private final float y;
    private float timeRemaining = 0.5f;

    private ScorePopup(String text, float x, float y) {
      this.text = text;
      this.x = x;
      this.y = y;
    }
  }

  /**
   * @return the player's score
   */
  public int getPlayerScore() {
    return score;
  }
}
