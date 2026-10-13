package io.github.goose_game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Main extends Game {
  private Batch batch;
  private BitmapFont font;

  @Override
  public void create() {
    batch = new SpriteBatch();
    font = new BitmapFont();

    setScreen(new GameScreen(this));
  }

  @Override
  public void dispose() {
    super.dispose();
    batch.dispose();
    font.dispose();
  }

  public Batch getBatch() {
    return batch;
  }

  public BitmapFont getFont() {
    return font;
  }
}
