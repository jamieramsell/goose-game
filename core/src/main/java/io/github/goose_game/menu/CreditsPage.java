package io.github.goose_game.menu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import io.github.goose_game.Main;
import io.github.goose_game.utils.DefaultSkin;

/**
 * Screen listing the credits and licences for the game's third-party assets.
 *
 * <p>Each credit is a wrapped, centred {@link Label} in the {@code credits} array in the
 * constructor; add an entry there to show another one. A {@code Back} button returns to the {@link
 * MainMenu}.
 *
 * <p>This screen owns a stage and skin, so {@link #dispose()} must be called when navigating away
 * from it.
 */
public class CreditsPage extends ScreenAdapter {

  private final Stage stage = new Stage(new ExtendViewport(640, 360));
  private final Skin skin;

  /**
   * Builds the credits page's widgets, layout and intro animation.
   *
   * @param game the game that owns this screen, used to return to the main menu
   */
  public CreditsPage(Main game) {
    this.skin = DefaultSkin.getSkin();

    // Page title
    Label title =
        new Label("Asset Credits", new Label.LabelStyle(skin.getFont("default"), Color.WHITE));

    // Credits
    Label[] credits = { // Add any credits within this array; they'll be drawn automatically.
      new Label(
          "Main menu background by heyheythere, <https://heyheythere.itch.io/parallax-skies-free / CC BY 4.0>",
          skin)
    };

    // Back button
    TextButton back = new TextButton("Back", skin);
    back.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent event, Actor actor) {
            game.setScreen(new MainMenu(game));
            dispose();
          }
        });

    // Menu layout
    Table table = new Table();

    table.setFillParent(true);
    table.add(title).padBottom(40).row();

    for (Label label : credits) {
      label.setWrap(true);
      label.setAlignment(Align.center);
      table.add(label).width(500).height(30).padBottom(20).row();
    }

    table.add(back).width(240).height(60).padTop(20).padBottom(16).row();

    stage.addActor(table);

    // Animations
    table.getColor().a = 0f;
    table.addAction(Actions.fadeIn(0.5f)); // fade the menu in
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
