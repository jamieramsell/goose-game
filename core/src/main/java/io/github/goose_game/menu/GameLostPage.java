package io.github.goose_game.menu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import io.github.goose_game.Main;
import io.github.goose_game.game.GameScreen;
import io.github.goose_game.state.GameStateManager;
import io.github.goose_game.utils.DefaultSkin;

public class GameLostPage extends ScreenAdapter {

  private final GameStateManager stateManager;
  private final GameScreen lostGame;

  private final Stage stage = new Stage(new ExtendViewport(640, 360));
  private final Skin skin;

  /**
   * Builds the game over menu's widgets, layout and intro animations.
   *
   * @param game the game that owns this screen
   * @param stateManager the current instance of {@link GameStateManager}
   * @param lostGame the game screen, drawn behind this menu
   */
  public GameLostPage(Main game, GameStateManager stateManager, GameScreen lostGame) {
    this.stateManager = stateManager;
    this.lostGame = lostGame;
    this.skin = DefaultSkin.getSkin();

    // Menu title
    Label title =
        new Label("You lost!", new Label.LabelStyle(skin.getFont("title"), Color.RED));

    // Reason label
    Label reason = new Label("You were swarmed by geese.",
        new Label.LabelStyle(skin.getFont("default"), Color.WHITE));
    reason.setAlignment(Align.center);

    // Score label
    Label score = new Label("Final score: " + lostGame.getPlayerScore(),
        new Label.LabelStyle(skin.getFont("default"), Color.WHITE));
    score.setAlignment(Align.center);

    // Quit to menu button
    TextButton menuQuit = new TextButton("Quit to menu", skin);
    menuQuit.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent event, Actor actor) {
            GameLostPage.this.stateManager.mainMenu(GameLostPage.this);
          }
        });

    // Quit to desktop button
    TextButton desktopQuit = new TextButton("Quit to desktop", skin);
    desktopQuit.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent event, Actor actor) {
            Gdx.app.exit();
          }
        });

    // Semi-transparent game overlay
    Drawable gameOverlay = skin.newDrawable("white", new Color(0, 0, 0, 0.6f));
    Image gameOverlayImage = new Image(gameOverlay);
    gameOverlayImage.setFillParent(true);
    stage.addActor(gameOverlayImage);

    // Menu layout
    Table table = new Table();

    table.setFillParent(true);
    table.add(title).padBottom(16).row();
    table.add(reason).width(240).height(60).padBottom(16).row();
    table.add(score).width(240).height(60).padBottom(40).row();
    table.add(menuQuit).width(240).height(60).padBottom(16).row();
    table.add(desktopQuit).width(240).height(60);

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
    lostGame.render(delta);

    stage.getViewport().apply();
    stage.act(delta);
    stage.draw();
  }

  @Override
  public void resize(int width, int height) {
    stage.getViewport().update(width, height, true);
    lostGame.resize(width, height);
  }

  @Override
  public void dispose() {
    stage.dispose();
    skin.dispose();
  }
}
