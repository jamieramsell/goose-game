package io.github.goose_game.utils;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;

/**
 * Factory for the game's default Scene2D UI {@link Skin}.
 *
 * <p>The skin is built entirely in code, so it needs no asset files. It is intended as a
 * placeholder until real button artwork is available. It registers the following resources, each
 * under the name {@code "default"} unless noted:
 *
 * <ul>
 *   <li>{@code "white"}: a 1x1 white {@link Texture}, tinted via {@link Skin#newDrawable} to draw
 *       solid-colour rectangles
 *   <li>a {@link BitmapFont} (libGDX's built-in font, scaled 2x)
 *   <li>a {@link TextButton.TextButtonStyle} with dark grey ({@code up}), grey ({@code over}) and
 *       black ({@code down}) backgrounds
 * </ul>
 *
 * <p>Because the style is named {@code "default"}, widgets constructed with just the skin, such as
 * {@code new TextButton("Play", skin)}, pick it up automatically.
 */
public class DefaultSkin {
  /**
   * Creates a new default skin.
   *
   * <p>Each call builds a new skin with its own native resources (texture and font). The caller
   * owns the returned skin and must call {@link Skin#dispose()} when finished, which also disposes
   * the texture and font that it contains.
   *
   * @return a new skin, containing the game's default UI styles
   */
  public static Skin getSkin() {
    Skin skin = new Skin();

    // 1x1 white pixel, tinted per state
    Pixmap pm = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
    pm.setColor(Color.WHITE);
    pm.fill();
    skin.add("white", new Texture(pm));
    pm.dispose();

    // Small default font
    BitmapFont font = new BitmapFont();
    font.getData().setScale(2f);
    skin.add("default", font);

    // Button styles for each stage
    TextButton.TextButtonStyle style = new TextButton.TextButtonStyle();
    style.up = skin.newDrawable("white", Color.DARK_GRAY);
    style.down = skin.newDrawable("white", Color.BLACK);
    style.over = skin.newDrawable("white", Color.GRAY);
    style.font = font;
    skin.add("default", style);

    return skin;
  }
}
