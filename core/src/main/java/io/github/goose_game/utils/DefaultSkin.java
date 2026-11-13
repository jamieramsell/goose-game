package io.github.goose_game.utils;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
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
 *   <li>a {@link BitmapFont} (libGDX's built-in font, scaled 2x) for titles and buttons
 *   <li>a {@link TextButton.TextButtonStyle} with dark grey ({@code up}), grey ({@code over}) and
 *       black ({@code down}) backgrounds
 *   <li>a second, unscaled {@link BitmapFont} and a white {@link Label.LabelStyle} that uses it,
 *       for body text such as credits
 * </ul>
 *
 * <p>Because the styles are named {@code "default"}, widgets constructed with just the skin, such
 * as {@code new TextButton("Play", skin)} or {@code new Label("text", skin)}, pick them up
 * automatically.
 */
public class DefaultSkin {
  /**
   * Creates a new default skin.
   *
   * <p>Each call builds a new skin with its own native resources (texture and fonts). The caller
   * owns the returned skin and must call {@link Skin#dispose()} when finished, which also disposes
   * the texture and fonts that it contains.
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

    // Default font
    BitmapFont font = new BitmapFont();
    font.getData().setScale(2f);
    skin.add("default", font);

    // Larger font for title text
    BitmapFont titleFont = new BitmapFont();
    titleFont.getData().setScale(3f);
    skin.add("title", titleFont);

    // Small font for text labels
    BitmapFont labelFont = new BitmapFont();
    labelFont.getData().setScale(1f);
    skin.add("small", labelFont);

    // Button styles for each stage
    TextButton.TextButtonStyle style = new TextButton.TextButtonStyle();
    style.up = skin.newDrawable("white", Color.DARK_GRAY);
    style.down = skin.newDrawable("white", Color.BLACK);
    style.over = skin.newDrawable("white", Color.GRAY);
    style.font = font;
    skin.add("default", style);

    // Text label style
    Label.LabelStyle labelStyle = new Label.LabelStyle(labelFont, Color.WHITE);
    skin.add("default", labelStyle);

    return skin;
  }
}
