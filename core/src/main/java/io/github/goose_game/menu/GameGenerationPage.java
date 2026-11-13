package io.github.goose_game.menu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import io.github.goose_game.Main;
import io.github.goose_game.utils.DefaultSkin;

/**
 * The loading screen shown while a new map is generated.
 *
 * <p>Shows a gently bobbing "Generating map..." message, which fades in, on a black background.
 *
 * <p>This screen owns native resources (the stage and skin), so {@link #dispose()} must be called
 * when navigating away from it.
 */
public class GameGenerationPage extends ScreenAdapter {
  private final Stage stage = new Stage(new ExtendViewport(640, 360));
  private final Skin skin;

  /**
   * Builds the page's message, layout and animations.
   *
   * <p>Must be called after libGDX has started, as it loads textures.
   *
   * @param game the game that owns this screen
   */
  public GameGenerationPage(Main game) {
    this.skin = DefaultSkin.getSkin();

    // 'Generating map' text
    Label title =
        new Label("Generating map...", new Label.LabelStyle(skin.getFont("default"), Color.WHITE));

    // Place text on page
    Table table = new Table();
    table.setFillParent(true);
    stage.addActor(table);
    table.add(title).padBottom(40).row();

    // Animate text
    table.getColor().a = 0f;
    table.addAction(Actions.fadeIn(0.5f)); // fade the menu in

    title.addAction(
        Actions.forever(
            Actions.sequence( // gentle bobbing title
                Actions.moveBy(0, 6, 1.5f, Interpolation.sine),
                Actions.moveBy(0, -6, 1.5f, Interpolation.sine))));
  }

  @Override
  public void show() {
    Gdx.input.setInputProcessor(stage);
  }

  @Override
  public void hide() {
    Gdx.input.setInputProcessor(null);
  }

  @Override
  public void render(float delta) {
    ScreenUtils.clear(0, 0, 0, 1);

    stage.getViewport().apply();
    stage.act(delta);
    stage.draw();
  }

  @Override
  public void resize(int width, int height) {
    stage.getViewport().update(width, height, true);
  }

  @Override
  public void dispose() {
    stage.dispose();
    skin.dispose();
  }
}
