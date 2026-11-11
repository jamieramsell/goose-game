package io.github.goose_game.menu;

import com.badlogic.gdx.Gdx;
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
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import io.github.goose_game.Main;
import io.github.goose_game.game.GameScreen;
import io.github.goose_game.utils.DefaultSkin;

public class MainMenu extends ScreenAdapter {

  private final Stage stage = new Stage(new ScreenViewport());
  private final Skin skin;

  public MainMenu(Main game) {
    this.skin = DefaultSkin.getSkin();

    // Game title
    Label title =
        new Label("Flock n Load", new Label.LabelStyle(skin.getFont("default"), Color.WHITE));

    // Play button
    TextButton play = new TextButton("Play", skin);
    play.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent event, Actor actor) {
            game.setScreen(new GameScreen(game));
            dispose();
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
    table.add(quit).width(240).height(60);

    stage.addActor(table);

    // Animations
    table.getColor().a = 0f;
    table.addAction(Actions.fadeIn(0.5f));                 // fade the menu in

    title.addAction(Actions.forever(Actions.sequence(      // gentle bobbing title
        Actions.moveBy(0, 6, 1.5f, Interpolation.sine),
        Actions.moveBy(0, -6, 1.5f, Interpolation.sine)
    )));
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
    ScreenUtils.clear(0.05f, 0.05f, 0.15f, 1);
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
