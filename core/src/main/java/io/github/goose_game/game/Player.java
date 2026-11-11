package io.github.goose_game.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import io.github.goose_game.Node;

public class Player extends Node {
  private Vector2 velocity;
  private final float speed;
  private float health;
  private Array<Animation<Sprite>> running;
  private Array<Animation<Sprite>> idle;
  private float frame;
  int i = 0;
  Camera camera;
  public Player(float x, float y, float width, float height, TextureAtlas atlas, float speed, float health, Camera camera) {
    super(x, y, width, height);
    this.speed = speed;
    this.health = health;
    this.velocity = new Vector2();
    this.camera = camera;

    this.running = new Array<>();
    this.idle = new Array<>();
    String[] directions = {"Left", "Right", "Back", "Front"};

    for(int i = 0; i < 4; i++){
      this.idle.add(new Animation<>(0.16f, atlas.createSprites(directions[i] + "Idle"), Animation.PlayMode.LOOP));
      this.running.add(new Animation<>(0.16f, atlas.createSprites(directions[i] + "Run"), Animation.PlayMode.LOOP));
    }
  }

  @Override
  public void update(float delta) {
     Vector2 direction = get_input();
     velocity.x += direction.x * speed;
     velocity.y += direction.y * speed;

     velocity.scl(0.65f);

     position.x += velocity.x * delta;
     position.y += velocity.y * delta;

     super.update(delta);
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
    if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
      ((Weapon) get_child("weapon_test")).attack();
    }
    dir = dir.nor();

    return dir;
  }

  @Override
  public void draw(Batch batch) {
    for (Node child : children.values()) {
      child.draw(batch);
    }

    frame += 2 * Gdx.graphics.getDeltaTime();
    if (velocity.x > 0.02f) {
      i = 1;
    }
    else if (velocity.x < -0.02f) {
      i = 0;
    }
    else if (velocity.y > 0.02f) {
      i = 2;
    }
    else if (velocity.y < -0.02f) {
      i = 3;
    }

    if (velocity.len() <= 0.02f) {
      Sprite currentFrame = idle.get(i).getKeyFrame(frame);
      currentFrame.setPosition(position.x - currentFrame.getWidth() / 2, position.y - currentFrame.getHeight() / 2);
      currentFrame.draw(batch);
    }
    else {
      Sprite currentFrame = running.get(i).getKeyFrame(frame);
      currentFrame.setPosition(position.x - currentFrame.getWidth() / 2, position.y - currentFrame.getHeight() / 2);
      currentFrame.draw(batch);
    }
  }
  public void damage(float damage) {
    health -= damage;
    System.out.println(health);
  }

  public Camera getCamera() {
    return camera;
  }
}
