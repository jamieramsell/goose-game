package io.github.goose_game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import io.github.goose_game.state.GameStateManager;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Main extends Game {
  private Batch batch;
  private BitmapFont font;
  private GameStateManager stateManager;

  @Override
  public void create() {
    batch = new SpriteBatch();
    font = new BitmapFont();
    stateManager = new GameStateManager(this);
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
