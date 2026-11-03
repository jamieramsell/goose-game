package io.github.goose_game;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

public class Enemy extends Node {
  Player target;
  float speed;
  Vector2 target_pos;
  float damage;
  int attackCounter;
  int attackDelay;
  boolean fleeing = false;
  public Enemy(float x, float y, float width, float height, Texture texture, Player target,  float speed, float damage, int attackDelay) {
    super(x, y, width, height, texture);
    this.target = target;
    this.speed = speed;
    this.damage = damage;
    this.attackCounter = 0;
    this.attackDelay = attackDelay;
    this.target_pos = target.position;
  }

  @Override
  public void update(float delta) {
    if (attackCounter != 0) {
      attackCounter += 1;
      if (attackCounter >= attackDelay) {
        attackCounter = 0;
      }
    }
    else if (overlaps(target)) {
      target.damage(damage);
      this.attackCounter += 1;
    }
    Vector2 dir = new Vector2(target_pos.x - position.x, target_pos.y - position.y);
    dir.nor();
    if (fleeing) {
      dir.x *= -1;
      dir.y *= -1;
      if (position.x - target.position.x > 320 || position.x - target.position.x < -320 || position.y - target.position.y > 320 || position.y - target.position.y < -320) {
        fleeing = false;
        position.x = target.position.x + new int[]{-1, 1}[MathUtils.random(0,1)] * 320;
        position.y = target.position.y + new int[]{-1, 1}[MathUtils.random(0,1)] * 320;
      }
    }
    position.x += dir.x * speed * delta;
    position.y += dir.y * speed * delta;
    super.update(delta);


  }

  public void disperse() {
    fleeing = true;
  }
}
