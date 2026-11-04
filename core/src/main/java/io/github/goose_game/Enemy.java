package io.github.goose_game;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

public class Enemy extends Node {
  Player target;
  float speed;
  Vector2 target_pos;
  Vector2 velocity = new Vector2(0,0);
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
    velocity.x *= 0.65F;
    velocity.y *= 0.65F;

    velocity.x += dir.x * speed * delta;
    velocity.y += dir.y * speed * delta;

    for (Node node: getParent().children.values()) {
      if (node instanceof Enemy) {
        if (position.dst(node.position) <= 32 && position.dst(node.position) != 0 && node != this) {
          Vector2 dirToNode = new Vector2(position.x - node.position.x, position.y - node.position.y);
          velocity.x += 2F * dirToNode.nor().x * speed * delta;
          velocity.y += 2F * dirToNode.nor().y * speed * delta;
        }
      }
    }


    velocity.clamp(0,1);

    position.x += velocity.x;
    position.y += velocity.y;
    super.update(delta);


  }

  public void disperse() {
    fleeing = true;
  }
}
