package io.github.goose_game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;

public class Player extends Node {
  private Vector2 velocity;
  private final float speed;
  public Player(float x, float y, float width, float height, Texture texture, float speed) {
    super(x, y, width, height, texture);
    this.speed = speed;
    this.velocity = new Vector2();
  }

  @Override
  public void update(float delta) {
     Vector2 direction = get_input();
     velocity.x += direction.x * speed;
     velocity.y += direction.y * speed;

     velocity.scl(0.65f);

     position.x += velocity.x * delta;
     position.y += velocity.y * delta;
  }

  private Vector2 get_input() {
    Vector2 dir = new Vector2();
    if(Gdx.input.isKeyPressed(Input.Keys.A)) {
      dir.x -= 1;
    }
    if(Gdx.input.isKeyPressed(Input.Keys.D)) {
      dir.x += 1;
    }
    if(Gdx.input.isKeyPressed(Input.Keys.S)) {
      dir.y -= 1;
    }
    if(Gdx.input.isKeyPressed(Input.Keys.W)) {
      dir.y += 1;
    }
    dir = dir.nor();

    return dir;
  }
}
