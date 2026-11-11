package io.github.goose_game.menu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.utils.Disposable;

/**
 * Animated parallax background for the main menu.
 *
 * <p>Six 320x180 layers are drawn back to front (sky, mountains, hills, trees, and two cloud
 * layers). Each layer scrolls horizontally at its own speed, in pixels per second, and wraps
 * seamlessly.
 *
 * <p>This class does not own a {@link Batch} or viewport; the caller supplies both when drawing. It
 * does own its textures, so {@link #dispose()} must be called when it is no longer needed.
 */
public class MenuBackground implements Disposable {

  private static final int WIDTH = 320;
  private static final int HEIGHT = 180;

  private final Texture[] layers = new Texture[6];
  private final float[] speeds = new float[6];
  private final float[] offsets = {0, 0, 0, 0, 0, 0};

  /**
   * Loads the layer textures from {@code assets/main_menu} and sets each layer's scroll speed.
   *
   * <p>Must be called after libGDX has started, as it creates textures.
   */
  public MenuBackground() {
    // Sky
    layers[0] = new Texture(Gdx.files.internal("main_menu/clear_day_sky.png"));
    speeds[0] = 0;

    // Mountains
    layers[1] = new Texture(Gdx.files.internal("main_menu/clear_day_far.png"));
    speeds[1] = 3;

    // Hills
    layers[2] = new Texture(Gdx.files.internal("main_menu/clear_day_mid.png"));
    speeds[2] = 6;

    // Trees
    layers[3] = new Texture(Gdx.files.internal("main_menu/clear_day_near.png"));
    speeds[3] = 12;

    // Far clouds
    layers[4] = new Texture(Gdx.files.internal("main_menu/clear_day_clouds_far.png"));
    speeds[4] = 2;

    // Near clouds
    layers[5] = new Texture(Gdx.files.internal("main_menu/clear_day_clouds_near.png"));
    speeds[5] = 4;

    // Set wrap mode for each layer
    for (Texture t : layers) {
      t.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.ClampToEdge);
    }
  }

  /**
   * Adjust positioning of each layer in the background art.
   *
   * @param delta the time in seconds that has passed since the previous frame
   */
  public void update(float delta) {
    for (int i = 0; i < layers.length; i++) {
      offsets[i] = (offsets[i] + speeds[i] * delta) % WIDTH;
    }
  }

  /**
   * Draws the background image.
   *
   * <p>Note that the caller is responsible for calling {@link Batch#begin()} before, and {@link
   * Batch#end()} after this method, and for setting the batch's projection matrix.
   *
   * @param batch the batch to draw with. The batch instance is shared, so it is not disposed of
   *     here.
   */
  public void draw(Batch batch) {
    for (int i = 0; i < layers.length; i++) {
      batch.draw(layers[i], 0, 0, WIDTH, HEIGHT, (int) offsets[i], 0, WIDTH, HEIGHT, false, false);
    }
  }

  @Override
  public void dispose() {
    for (Texture t : layers) {
      t.dispose();
    }
  }
}
