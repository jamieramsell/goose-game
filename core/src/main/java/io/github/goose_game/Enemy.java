package io.github.goose_game;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;

public class Enemy extends Node {
  Player target;
  float speed;
  float damage;
  public Enemy(float x, float y, float width, float height, Texture texture, Player target,  float speed, float damage) {
    super(x, y, width, height, texture);
    this.target = target;
    this.speed = speed;
    this.damage = damage;
  }

  @Override
  public void update(float delta) {
    Vector2 dir = new Vector2(target.position.x - position.x, target.position.y - position.y);
    dir.nor();
    position.x += dir.x * speed * delta;
    position.y += dir.y * speed * delta;
    super.update(delta);
    if (overlaps(target)) {
      target.damage(damage);
    }
  }
}
