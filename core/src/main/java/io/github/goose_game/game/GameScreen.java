package io.github.goose_game.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import io.github.goose_game.Main;
import io.github.goose_game.mapgen.TileMap;
import io.github.goose_game.mapgen.TileMapRenderer;
import io.github.goose_game.state.GameState;
import io.github.goose_game.state.GameStateManager;
import io.github.goose_game.utils.Node;

public class GameScreen extends ScreenAdapter {

  private static final float WORLD_WIDTH = 320f;
  private static final float WORLD_HEIGHT = 320f;

  private final Main game;
  private final GameStateManager stateManager;
  private final Batch batch;
  private final TextureAtlas atlas;

  private final Viewport viewport = new ExtendViewport(WORLD_WIDTH, WORLD_HEIGHT);
  private final OrthographicCamera camera = new OrthographicCamera();

  private final Animation<Sprite> stickAttackTexture;
  private final Node root = new Node(0, 0, 0, 0);
  private final ShapeRenderer debugShapeRenderer = new ShapeRenderer();

  private final TileMap map;
  private final TileMapRenderer renderer;

  private Texture background = new Texture(Gdx.files.internal("floor_test.png"));
  private final Texture seedTexture;
  private final SeedWeapon seedWeapon;
  private Sprite playerTexture;
  private boolean showEntityBounds;

  Node player;

  public GameScreen(Main game, GameStateManager stateManager, TileMap map) {
    this.game = game;
    this.stateManager = stateManager;
    this.batch = game.getBatch();
    this.atlas = new TextureAtlas(Gdx.files.internal("spritesheet.atlas"));
    this.seedTexture = new Texture(Gdx.files.internal("seeds.png"));
    this.seedTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);

    this.map = map;
    this.renderer = new TileMapRenderer(this.map);

    for (Texture texture : atlas.getTextures()) {
      texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
    }

    playerTexture = atlas.createSprite("player_test");

    this.stickAttackTexture =
        new Animation<>(1, atlas.createSprites("sheet_6_katana_slash"), Animation.PlayMode.NORMAL);

    float spawnX = map.getSpawnPosition().x;
    float spawnY = map.getSpawnPosition().y;

    this.root.add_child("player", new Player(spawnX, spawnY, 16, 20, atlas, map, 64, 200, camera));

    this.root.add_child(
        "enemyManager", new EnemyManager(atlas, (Player) this.root.get_child("player"), map));

    this.root.get_child("player")
        .add_child(
            "weapon_test", new Weapon(0, 0, 32, 32, stickAttackTexture, atlas, 1, 20, 5, 120));

    this.seedWeapon = new SeedWeapon(new Sprite(seedTexture));
    this.root.get_child("player").add_child("seed_weapon", seedWeapon);

    viewport.setCamera(camera);
    camera.position.set(spawnX, spawnY, 0);
  }

  @Override
  public void resize(int width, int height) {
    viewport.update(width, height, false);
  }

  @Override
  public void render(float delta) {
    ScreenUtils.clear(0, 0, 0.1f, 1);
    update(delta);

    viewport.apply();
    batch.setProjectionMatrix(viewport.getCamera().combined);
    batch.begin();
    renderer.render(batch, camera);
    root.draw(batch);

    batch.end();

    batch.setProjectionMatrix(
        new Matrix4().setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight()));
    batch.begin();
    game.getFont().draw(batch, "Seeds: " + seedWeapon.getAmmo(), 12, Gdx.graphics.getHeight() - 12);
    batch.end();

    if (showEntityBounds) {
      debugShapeRenderer.setProjectionMatrix(camera.combined);
      debugShapeRenderer.begin(ShapeRenderer.ShapeType.Line);
      debugShapeRenderer.setColor(Color.GREEN);
      root.drawDebugBounds(debugShapeRenderer);
      debugShapeRenderer.end();
    }
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

      // Only update nodes while game is active.
      root.update(delta);
      Node player = root.get_child("player");
      camera.position.lerp(new Vector3(player.position.x, player.position.y, 0), 0.02f);
    }
  }

  @Override
  public void dispose() {
    debugShapeRenderer.dispose();
    seedTexture.dispose();
    renderer.dispose();
  }
}
