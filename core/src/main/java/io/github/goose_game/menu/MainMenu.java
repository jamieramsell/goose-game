package io.github.goose_game.menu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.FillViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import io.github.goose_game.Main;
import io.github.goose_game.game.GameScreen;
import io.github.goose_game.state.GameStateManager;
import io.github.goose_game.utils.DefaultSkin;

/**
 * The game's main menu screen.
 *
 * <p>Shows the title, a {@code Play}, {@code Credits} and {@code Quit} button over an animated
 * parallax {@link MenuBackground}. The background and the UI each use their own {@link Viewport},
 * so that the background fills the window, while the buttons scale uniformly.
 *
 * <p>This screen owns native resources (the stage, skin and background), so {@link #dispose()} must
 * be called when navigating away from it. The shared {@link Batch} belongs to {@link Main} and is
 * not disposed here.
 */
public class MainMenu extends ScreenAdapter {

  private final GameStateManager stateManager;

  private final Stage stage = new Stage(new ExtendViewport(640, 360));
  private final Skin skin;

  private final MenuBackground background = new MenuBackground();
  private final Viewport bgViewport = new FillViewport(320, 180);
  private final Batch batch;

  /**
   * Builds the menu's widgets, layout and intro animations.
   *
   * <p>The game instance is used to switch screens from the buttons and to obtain the shared batch
   * used to draw the background. Must be called after libGDX has started, as it loads textures.
   *
   * @param game the game that owns this screen
   */
  public MainMenu(Main game, GameStateManager stateManager) {
    this.stateManager = stateManager;
    this.skin = DefaultSkin.getSkin();
    this.batch = game.getBatch();

    // Game title
    Label title =
        new Label("Flock n Load", new Label.LabelStyle(skin.getFont("default"), Color.WHITE));

    // Play button
    TextButton play = new TextButton("Play", skin);
    play.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent event, Actor actor) {
            stateManager.play(MainMenu.this);
          }
        });

    // Credits button
    TextButton credits = new TextButton("Credits", skin);
    credits.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent event, Actor actor) {
            stateManager.credits(MainMenu.this);
          }
        });

    // Quit button
    TextButton quit = new TextButton("Quit", skin);
    quit.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent event, Actor actor) {
            Gdx.app.exit();
          }
        });

    // Menu layout
    Table table = new Table();

    table.setFillParent(true);
    table.add(title).padBottom(40).row();
    table.add(play).width(240).height(60).padBottom(16).row();
    table.add(credits).width(240).height(60).padBottom(16).row();
    table.add(quit).width(240).height(60);

    stage.addActor(table);

    // Animations
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

    background.update(delta);
    bgViewport.apply();
    batch.setProjectionMatrix(bgViewport.getCamera().combined);
    batch.begin();
    background.draw(batch);
    batch.end();

    stage.getViewport().apply();
    stage.act(delta);
    stage.draw();
  }

  @Override
  public void resize(int width, int height) {
    bgViewport.update(width, height, true);
    stage.getViewport().update(width, height, true);
  }

  @Override
  public void dispose() {
    background.dispose();
    stage.dispose();
    skin.dispose();
  }
}
