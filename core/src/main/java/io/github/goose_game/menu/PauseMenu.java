package io.github.goose_game.menu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
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
import io.github.goose_game.Main;
import io.github.goose_game.game.GameScreen;
import io.github.goose_game.state.GameState;
import io.github.goose_game.state.GameStateManager;
import io.github.goose_game.utils.DefaultSkin;

public class PauseMenu extends ScreenAdapter {

  private final GameStateManager stateManager;
  private final GameScreen pausedGame;

  private final Stage stage = new Stage(new ExtendViewport(640, 360));
  private final Skin skin;

  public PauseMenu(Main game, GameStateManager stateManager, GameScreen pausedGame) {
    this.stateManager = stateManager;
    this.pausedGame = pausedGame;
    this.skin = DefaultSkin.getSkin();

    // Menu title
    Label title =
        new Label("Game Paused", new Label.LabelStyle(skin.getFont("default"), Color.WHITE));

    // Resume button
    TextButton resume = new TextButton("Resume Game", skin);
    resume.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent event, Actor actor) {
            stateManager.play(PauseMenu.this);
          }
        });

    // Quit to menu button
    TextButton menuQuit = new TextButton("Quit to menu", skin);
    menuQuit.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent event, Actor actor) {
            stateManager.mainMenu(PauseMenu.this);
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

    // Menu layout
    Table table = new Table();

    table.setFillParent(true);
    table.add(title).padBottom(40).row();
    table.add(resume).width(240).height(60).padBottom(16).row();
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
    ScreenUtils.clear(0, 0, 0, 1);
    update();

    stage.getViewport().apply();
    stage.act(delta);
    stage.draw();
  }

  private void update() {
    if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)
        && stateManager.getGameState() == GameState.PAUSED) {
      stateManager.play(this);
    }
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
