package io.github.goose_game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import sun.jvm.hotspot.debugger.cdbg.CDebugger;

import static sun.security.util.Debug.println;

public class GameScreen extends ScreenAdapter {
  private static final float WORLD_WIDTH = 320f;
  private static final float WORLD_HEIGHT = 320f;


  private final Main game;
  private final Batch batch;
  private final Viewport viewport = new ExtendViewport(WORLD_WIDTH, WORLD_HEIGHT);
  private final OrthographicCamera camera = new OrthographicCamera();
  private final Texture background = new Texture(Gdx.files.internal("floor_test.png"));
  private final Texture playerTexture = new Texture(Gdx.files.internal("player_test.png"));
  Entity player;

  public GameScreen(Main game) {
    this.game = game;
    this.batch = game.getBatch();
    this.player = new Player(0,0, 32, 32, playerTexture, 64);
    viewport.setCamera(camera);

    background.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
  }

  @Override
  public void resize(int width, int height) {
    viewport.update(width, height, false);
  }

  @Override
  public void render(float delta) {
    ScreenUtils.clear(0, 0, 0, 1);
    update(delta);

    viewport.apply();
    batch.setProjectionMatrix(viewport.getCamera().combined);
    batch.begin();

    batch.draw(background,0 , 0, 0, 0, 32 * 4, 32 * 4);
    player.draw(batch);

    batch.end();
  }

  private void update(float delta) {
    player.update(delta);
    camera.position.lerp(new Vector3(player.position.x, player.position.y, 0), 0.01f);
  }
}
