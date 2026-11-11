package io.github.goose_game.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import io.github.goose_game.Main;
import io.github.goose_game.Node;

public class GameScreen extends ScreenAdapter {
  private static final float WORLD_WIDTH = 320f;
  private static final float WORLD_HEIGHT = 320f;


  private final Main game;
  private final Batch batch;
  private final TextureAtlas atlas;
  private final Viewport viewport = new ExtendViewport(WORLD_WIDTH, WORLD_HEIGHT);
  private final OrthographicCamera camera = new OrthographicCamera();
  private Texture background = new Texture(Gdx.files.internal("floor_test.png"));
  private Sprite playerTexture;
  private final Animation<Sprite> stickAttackTexture;
  private final Node root = new Node(0,0,0,0);
  private final ShapeRenderer debugShapeRenderer = new ShapeRenderer();
  private boolean showEntityBounds;
  Node player;

  public GameScreen(Main game) {
    this.game = game;
    this.batch = game.getBatch();
    this.atlas = new TextureAtlas(Gdx.files.internal("spritesheet.atlas"));
    for (Texture texture: atlas.getTextures()) {
      texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
    }
    playerTexture = atlas.createSprite("player_test");
    this.stickAttackTexture = new Animation<>(1, atlas.createSprites("sheet_6_katana_slash"), Animation.PlayMode.NORMAL);
    this.root.add_child("player", new Player(8,6, 16, 20, atlas, 64, 200, camera));
    this.root.add_child("enemyManager", new EnemyManager(atlas,(Player) this.root.get_child("player")));
    this.root.get_child("player").add_child("weapon_test",  new Weapon(0, 0, 32, 32, stickAttackTexture, atlas, 1, 20, 5, 120));
    viewport.setCamera(camera);

    background.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
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

    batch.draw(background,-32 * 64 , -32 * 64, 0, 0, 32 * 128, 32 * 128);
    root.draw(batch);

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
    root.update(delta);
    Node player = root.get_child("player");
    camera.position.lerp(new Vector3(player.position.x, player.position.y, 0), 0.02f);
  }

  @Override
  public void dispose() {
    debugShapeRenderer.dispose();
  }
}
