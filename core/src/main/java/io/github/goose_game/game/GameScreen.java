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
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import io.github.goose_game.Main;
import io.github.goose_game.state.GameState;
import io.github.goose_game.state.GameStateManager;
import io.github.goose_game.utils.DefaultSkin;
import io.github.goose_game.utils.Node;

public class GameScreen extends ScreenAdapter {

  private static final float WORLD_WIDTH = 320f;
  private static final float WORLD_HEIGHT = 320f;
  private static final float GAME_DURATION_SECONDS = 5 * 60;

  private final Main game;
  private final GameStateManager stateManager;
  private final Batch batch;
  private final TextureAtlas atlas;
  private final Stage hudStage = new Stage(new ScreenViewport());
  private final BitmapFont timerFont = new BitmapFont();
  private final BitmapFont victoryFont = new BitmapFont();
  private final Skin victorySkin = DefaultSkin.getSkin();
  private final Label timerLabel;
  private final Label escapedLabel;
  private final Table victoryTable;

  private final Viewport viewport = new ExtendViewport(WORLD_WIDTH, WORLD_HEIGHT);
  private final OrthographicCamera camera = new OrthographicCamera();

  private final Animation<Sprite> stickAttackTexture;
  private final Node root = new Node(0, 0, 0, 0);
  private final ShapeRenderer debugShapeRenderer = new ShapeRenderer();
  private Texture background = new Texture(Gdx.files.internal("floor_test.png"));
  private Sprite playerTexture;
  private boolean showEntityBounds;
  private float remainingTime = GAME_DURATION_SECONDS;
  private int displayedSeconds = -1;

  Node player;

  public GameScreen(Main game, GameStateManager stateManager) {
    this.game = game;
    this.stateManager = stateManager;
    this.batch = game.getBatch();
    this.atlas = new TextureAtlas(Gdx.files.internal("spritesheet.atlas"));

    timerFont.getData().setScale(4f);
    victoryFont.getData().setScale(8f);
    timerLabel = new Label("", new Label.LabelStyle(timerFont, Color.WHITE));
    escapedLabel = new Label("You escaped!", new Label.LabelStyle(victoryFont, Color.WHITE));

    Table timerTable = new Table();
    timerTable.setFillParent(true);
    timerTable.top();
    timerTable.add(timerLabel).padTop(12);
    hudStage.addActor(timerTable);

    TextButton mainMenuButton = new TextButton("Return to Main Menu", victorySkin);
    mainMenuButton.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent event, Actor actor) {
            stateManager.mainMenu(GameScreen.this);
          }
        });

    victoryTable = new Table();
    victoryTable.setFillParent(true);
    victoryTable.center();
    victoryTable.add(escapedLabel).padBottom(24).row();
    victoryTable.add(mainMenuButton).width(260).height(64);
    victoryTable.setVisible(false);
    hudStage.addActor(victoryTable);

    for (Texture texture : atlas.getTextures()) {
      texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
    }

    playerTexture = atlas.createSprite("player_test");

    this.stickAttackTexture =
        new Animation<>(1, atlas.createSprites("sheet_6_katana_slash"), Animation.PlayMode.NORMAL);

    this.root.add_child("player", new Player(8, 6, 16, 20, atlas, 64, 200, camera));

    this.root.add_child(
        "enemyManager", new EnemyManager(atlas, (Player) this.root.get_child("player")));

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

    batch.end();

    if (showEntityBounds) {
      debugShapeRenderer.setProjectionMatrix(camera.combined);
      debugShapeRenderer.begin(ShapeRenderer.ShapeType.Line);
      debugShapeRenderer.setColor(Color.GREEN);
      root.drawDebugBounds(debugShapeRenderer);
      debugShapeRenderer.end();
    }

    hudStage.act(delta);
    hudStage.draw();
  }

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
        remainingTime = Math.max(0, remainingTime - delta);
        updateTimerLabel();

        if (remainingTime == 0) {
          timerLabel.setVisible(false);
          victoryTable.setVisible(true);
          Gdx.input.setInputProcessor(hudStage);
          stateManager.victory();
        } else {
          /* Only update nodes while game is active */ 
          root.update(delta);
          Node player = root.get_child("player");
          camera.position.lerp(new Vector3(player.position.x, player.position.y, 0), 0.02f);
        }
      }
    }
  }

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
    victoryFont.dispose();
    victorySkin.dispose();
  }
}
