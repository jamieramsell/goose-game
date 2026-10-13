package io.github.goose_game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class GameScreen extends ScreenAdapter {
  private static final float WORLD_WIDTH = 16f;
  private static final float WORLD_HEIGHT = 16f;


  private final Main game;
  private final Batch batch;
  private final Viewport viewport = new ExtendViewport(WORLD_WIDTH, WORLD_HEIGHT);
  private final Texture background = new Texture(Gdx.files.internal("floor_test.png"));

  public GameScreen(Main game) {
    this.game = game;
    this.batch = game.getBatch();

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

    float u2 = viewport.getScreenWidth()/ WORLD_WIDTH;
    float v2 = viewport.getScreenHeight()/ WORLD_HEIGHT;
    batch.draw(background, 0, 0, viewport.getScreenWidth(), viewport.getScreenHeight(), 0, 0, u2, v2);

    batch.end();
  }

  private void update(float delta) {
    viewport.getCamera().position.x += 1f * delta;
    viewport.getCamera().position.y += 1f * delta;
    viewport.getCamera().update();
  }
}
