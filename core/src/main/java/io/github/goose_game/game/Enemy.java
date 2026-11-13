package io.github.goose_game.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import io.github.goose_game.utils.Node;

public class Enemy extends Node {
  private static final float SEED_EAT_DURATION = 1.25f;

  Player target;
  float speed;
  Vector2 target_pos;
  Vector2 velocity = new Vector2(0, 0);
  private Array<Animation<Sprite>> animations;
  float damage;
  int attackCounter;
  int attackDelay;
  private float frame;
  boolean fleeing = false;
  private int health;
  private int maxHealth;
  private int i = 0;
  private boolean facingRight = true;
  private SeedProjectile eatingSeed;
  private float seedEatTimeRemaining;

  public Enemy(
      float x,
      float y,
      float width,
      float height,
      TextureAtlas atlas,
      Player target,
      float speed,
      float damage,
      int attackDelay,
      int health) {
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
    for (int i = 0; i < 4; i++) {
      if (i < 2) {
        this.animations.add(
            new Animation<>(
                0.16f, atlas.createSprites("GooseWalk" + directions[i]), Animation.PlayMode.LOOP));
      } else {
        this.animations.add(
            new Animation<>(
                0.16f,
                atlas.createSprites("GooseAttack" + directions[i - 2]),
                Animation.PlayMode.LOOP));
      }
    }
  }

  @Override
  public void update(float delta) {
    EnemyManager enemyManager = (EnemyManager) getParent();
    if (eatingSeed != null) {
      if (eatingSeed.isConsumed()) {
        eatingSeed = null;
        seedEatTimeRemaining = 0;
      } else {
        velocity.setZero();
        seedEatTimeRemaining -= delta;
        if (seedEatTimeRemaining <= 0) {
          eatingSeed.consume();
          eatingSeed = null;
        }
        super.update(delta);
        return;
      }
    }

    SeedProjectile seedTarget = fleeing ? null : enemyManager.getClosestSeed(position);
    boolean attractedToSeed = seedTarget != null;

    if (attackCounter != 0) {
      attackCounter += 1;
      if (attackCounter >= attackDelay) {
        attackCounter = 0;
      }
    } else if (!attractedToSeed && overlaps(target)) {
      target.damage(damage);
      System.out.println("Quack!");
      this.attackCounter += 1;
    }
    Vector2 movementTarget = attractedToSeed ? seedTarget.position : target.position;
    if (attractedToSeed) {
      updateFacing(movementTarget.x - position.x);
    }
    Vector2 dir = movementTarget.cpy().sub(position).nor();
    if (fleeing) {
      dir.x *= -1;
      dir.y *= -1;
      if (position.x - target.position.x > 320
          || position.x - target.position.x < -320
          || position.y - target.position.y > 320
          || position.y - target.position.y < -320) {
        fleeing = false;
        position.x = target.position.x + new int[] {-1, 1}[MathUtils.random(0, 1)] * 320;
        position.y = target.position.y + new int[] {-1, 1}[MathUtils.random(0, 1)] * 320;
        health = maxHealth;
      }
    }
    if (attractedToSeed && position.dst2(seedTarget.position) <= 12 * 12) {
      eatingSeed = seedTarget;
      seedEatTimeRemaining = SEED_EAT_DURATION;
      velocity.setZero();
      super.update(delta);
      return;
    }

    velocity.x *= 0.65F;
    velocity.y *= 0.65F;

    velocity.x += dir.x * speed;
    velocity.y += dir.y * speed;

    for (Node node : getParent().getChildren().values()) {
      if (node instanceof Enemy) {
        if (position.dst(node.position) <= 32 && position.dst(node.position) != 0 && node != this) {
          Vector2 dirToNode =
              new Vector2(position.x - node.position.x, position.y - node.position.y);
          velocity.x += 1F * dirToNode.nor().x * speed;
          velocity.y += 1F * dirToNode.nor().y * speed;
        }
      }
    }

    if (overlaps(target) && !fleeing && !attractedToSeed) {
      velocity = new Vector2(0, 0);
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
    SeedProjectile seedTarget =
        fleeing ? null : ((EnemyManager) getParent()).getClosestSeed(position);
    if (seedTarget != null) {
      updateFacing(seedTarget.position.x - position.x);
    } else if (target.position.x != position.x) {
      facingRight = target.position.x > position.x;
    }
    i = facingRight ? 1 : 0;
    if (!fleeing && attackCounter != 0) {
      i += 2;
    }

    if (fleeing) {
      i = facingRight ? 0 : 1;
    }

    Sprite currentFrame = animations.get(i).getKeyFrame(frame);
    currentFrame.setPosition(
        position.x - currentFrame.getWidth() / 2, position.y - currentFrame.getHeight() / 2);
    currentFrame.draw(batch);
    for (Node child : children.values()) {
      child.draw(batch);
    }
  }

  private void updateFacing(float horizontalDirection) {
    if (horizontalDirection != 0) {
      facingRight = horizontalDirection > 0;
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
