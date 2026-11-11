package io.github.goose_game.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import io.github.goose_game.Node;

public class Enemy extends Node {
  Player target;
  float speed;
  Vector2 target_pos;
  Vector2 velocity = new Vector2(0,0);
  private Array<Animation<Sprite>> animations;
  float damage;
  int attackCounter;
  int attackDelay;
  private float frame;
  boolean fleeing = false;
  private int health;
  private int maxHealth;
  private int i = 0;
  public Enemy(float x, float y, float width, float height, TextureAtlas atlas, Player target, float speed, float damage, int attackDelay, int health) {
    super(x, y, width, height);
    this.target = target;
    this.speed = speed;
    this.damage = damage;
    this.attackCounter = 0;
    this.attackDelay = attackDelay;
    this.target_pos = target.position;
    this.health = health;
    this.maxHealth = health;
    this.animations = new Array<>();

    String[] directions = {"Left", "Right"};
    for(int i = 0; i < 4; i++){
      if (i < 2) {
        this.animations.add(new Animation<>(0.16f, atlas.createSprites("GooseWalk" + directions[i]), Animation.PlayMode.LOOP));
      } else {
        this.animations.add(new Animation<>(0.16f, atlas.createSprites("GooseAttack" + directions[i - 2]), Animation.PlayMode.LOOP));
      }
    }
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
      System.out.println("Quack!");
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
        health = maxHealth;
      }
    }
    velocity.x *= 0.65F;
    velocity.y *= 0.65F;

    velocity.x += dir.x * speed;
    velocity.y += dir.y * speed;



    for (Node node : getParent().getChildren().values()) {
      if (node instanceof Enemy) {
        if (position.dst(node.position) <= 32 && position.dst(node.position) != 0 && node != this) {
          Vector2 dirToNode = new Vector2(position.x - node.position.x, position.y - node.position.y);
          velocity.x += 1F * dirToNode.nor().x * speed;
          velocity.y += 1F * dirToNode.nor().y * speed;
        }
      }
    }


    if (overlaps(target) && !fleeing) {
      velocity = new Vector2(0,0);
    }
    if (fleeing) {
        velocity.x *= 1.1F;
        velocity.y *= 1.1F;
    }

    position.x += velocity.x * delta;
    position.y += velocity.y * delta;
    super.update(delta);



  }

  @Override
  public void draw(Batch batch) {
    frame += 2 * Gdx.graphics.getDeltaTime();
    if (target.position.x > position.x) {
      i = 1;
    }
    if (target.position.x < position.x) {
      i = 0;
    }
    if (!fleeing && attackCounter != 0) {
      i += 2;
    }

    if (fleeing) {
      i = (i == 0) ? 1 : 0;
    }

    Sprite currentFrame = animations.get(i).getKeyFrame(frame);
    currentFrame.setPosition(position.x - currentFrame.getWidth() / 2, position.y - currentFrame.getHeight() / 2);
    currentFrame.draw(batch);
    for (Node child : children.values()) {
      child.draw(batch);
    }
  }

  public void disperse() {
    fleeing = true;
  }

  public void damage(int damage) {
    health -= damage;
    if (health <= 0) {
      fleeing = true;
    }
  }
}
