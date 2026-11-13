package io.github.goose_game.utils;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.BaseDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;

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

  /** Default background colour of a TextField, darker than DARK_GRAY */
  private final static Color TEXT_FIELD_FILL = new Color(0.08f, 0.08f, 0.08f, 1f);

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

    // Smaller font for text labels and inputs
    BitmapFont smallerFont = new BitmapFont();
    smallerFont.getData().setScale(1f);

    // Button styles for each stage
    TextButton.TextButtonStyle buttonStyle = getButtonStyle(skin, font);
    skin.add("default", buttonStyle);

    // Text label style
    Label.LabelStyle labelStyle = new Label.LabelStyle(smallerFont, Color.WHITE);
    skin.add("default", labelStyle);

    // Text field style 
    TextField.TextFieldStyle textFieldStyle = getDefaultTextFieldStyle(skin, smallerFont);
    skin.add("default", textFieldStyle);

    // Hover style for the text field
    TextField.TextFieldStyle hoverStyle = getHoverTextFieldStyle(skin, textFieldStyle);
    skin.add("hover", hoverStyle);

    return skin;
  }

  private static TextButton.TextButtonStyle getButtonStyle(Skin skin, BitmapFont font) {
    TextButton.TextButtonStyle style = new TextButton.TextButtonStyle();
    style.up = skin.newDrawable("white", Color.DARK_GRAY);
    style.down = skin.newDrawable("white", Color.BLACK);
    style.over = skin.newDrawable("white", Color.GRAY);
    style.font = font;

    return style;
  }

  private static TextField.TextFieldStyle getDefaultTextFieldStyle(Skin skin, BitmapFont font) {
    BaseDrawable cursor = (BaseDrawable) skin.newDrawable("white", Color.WHITE);
    cursor.setMinWidth(2);

    TextField.TextFieldStyle textFieldStyle = new TextField.TextFieldStyle();
    textFieldStyle.font = font;
    textFieldStyle.fontColor = Color.WHITE;
    textFieldStyle.messageFont = font;
    textFieldStyle.messageFontColor = Color.GRAY; // dim hint text
    textFieldStyle.background = box(skin, "fieldBox", Color.GRAY, TEXT_FIELD_FILL);
    textFieldStyle.focusedBackground = box(skin, "fieldBoxFocused", Color.WHITE, TEXT_FIELD_FILL);
    textFieldStyle.cursor = cursor;
    textFieldStyle.selection = skin.newDrawable("white", new Color(1f, 1f, 1f, 0.3f));

    return textFieldStyle;
  }

  private static TextField.TextFieldStyle getHoverTextFieldStyle(Skin skin, TextField.TextFieldStyle defaultStyle) {
    TextField.TextFieldStyle hoverStyle = new TextField.TextFieldStyle(defaultStyle);
    hoverStyle.background = box(skin, "fieldBoxHover", Color.LIGHT_GRAY, TEXT_FIELD_FILL);
    skin.add("hover", hoverStyle);
    
    return hoverStyle;
  }

  /** A box with a solid border and a flat fill, which stretches to any size. */
  private static Drawable box(Skin skin, String name, Color border, Color fill) {
    int borderSize = 2; // border thickness in pixels

    Pixmap pixmap = new Pixmap(borderSize * 2 + 1, borderSize * 2 + 1, Pixmap.Format.RGBA8888);
    pixmap.setColor(border);
    pixmap.fill();
    pixmap.setColor(fill);
    pixmap.fillRectangle(borderSize, borderSize, 1, 1);

    Texture tex = new Texture(pixmap);
    pixmap.dispose();

    tex.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
    skin.add(name, tex); // registered so skin.dispose() cleans it up

    NinePatch patch = new NinePatch(new TextureRegion(tex), borderSize, borderSize, borderSize, borderSize);
    patch.setPadding(12, 12, 8, 8); // keeps text off the border

    return new NinePatchDrawable(patch);
  }
}
