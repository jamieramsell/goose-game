package io.github.goose_game.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import io.github.goose_game.mapgen.MapGenerator;
import io.github.goose_game.mapgen.TileMap;
import io.github.goose_game.utils.Node;
import java.util.function.Consumer;

public class Enemy extends Node {
  private final TileMap map;
  private final EnemyManager enemyManager;
  Player target;

  private int health;
  private int maxHealth;
  float speed;
  Vector2 target_pos;
  Vector2 velocity = new Vector2(0, 0);

  private Array<Animation<Sprite>> animations;
  private float frame;

  float damage;
  int attackCounter;
  int attackDelay;
  
  boolean fleeing = false;
  private int i = 0;
  private final float spriteScale;
  private final boolean despawnOnDefeat;
  private final Consumer<Enemy> onDispersed;

  public Enemy(
      float x,
      float y,
      float width,
      float height,
      TextureAtlas atlas,
      Player target,
      TileMap map,
      EnemyManager enemyManager,
      float speed,
      float damage,
      int attackDelay,
      int health,
      float spriteScale,
      boolean despawnOnDefeat,
      Consumer<Enemy> onDispersed
  ) {
    super(x, y, width, height);
    this.target = target;
    this.enemyManager = enemyManager;
    this.speed = speed;
    this.damage = damage;
    this.attackCounter = 0;
    this.attackDelay = attackDelay;
    this.target_pos = target.position;
    this.health = health;
    this.maxHealth = health;
    this.spriteScale = spriteScale;
    this.despawnOnDefeat = despawnOnDefeat;
    this.onDispersed = onDispersed;
    this.animations = new Array<>();
    this.map = map;

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
    if (attackCounter != 0) {
      attackCounter += 1;
      if (attackCounter >= attackDelay) {
        attackCounter = 0;
      }
    } else if (overlaps(target)) {
      target.damage(damage);
      System.out.println("Quack!");
      this.attackCounter += 1;
    }
    
    Vector2 dir = new Vector2(target_pos.x - position.x, target_pos.y - position.y);
    dir.nor();
    if (fleeing) {
      dir.x *= -1;
      dir.y *= -1;
      if (position.x - target.position.x > 320
          || position.x - target.position.x < -320
          || position.y - target.position.y > 320
          || position.y - target.position.y < -320) {
        if (despawnOnDefeat) {
          delete_self();
          return;
        }
        fleeing = false;
        enemyManager.teleportEnemyNearPlayer(this);
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
          Vector2 dirToNode =
              new Vector2(position.x - node.position.x, position.y - node.position.y);
          velocity.x += 1F * dirToNode.nor().x * speed;
          velocity.y += 1F * dirToNode.nor().y * speed;
        }
      }
    }

    if (overlaps(target) && !fleeing) {
      velocity = new Vector2(0, 0);
    }
    if (fleeing) {
      velocity.x *= 1.3F;
      velocity.y *= 1.3F;
    }

    float targetX = position.x + velocity.x * delta;
    float targetY = position.y + velocity.y * delta;

    /*
     * Validate that each direction is within bounds, unless the goose is fleeing. Fleeing geese can 
     * go anywhere.
     *
     * Dev note: this is done seperately so that, for example, if the goose is moving downwards and
     * to the right when there is a wall on their right hand side, they still move downwards, rather
     * than not at all.
     *
     * When a goose collides with a wall, reset velocity in that direction to prevent a 'sticky
     * wall' effect, where the goose cannot move as their velocity is still positive in the
     * direction of the wall.
     */
    if (fleeing) {
      position.x = targetX;
      position.y = targetY;
      super.update(delta);
      return;
    }
    
    if (isTargetPosInBounds(targetX) && !map.isPixelSolidForGeese((int) targetX, (int) position.y)) {
      position.x = targetX;
    } else {
      velocity.x = 0;
    }

    if (isTargetPosInBounds(targetY) && !map.isPixelSolidForGeese((int) position.x, (int) targetY)) {
      position.y = targetY;
    } else {
      velocity.y = 0;
    }

    super.update(delta);
  }

  private boolean isTargetPosInBounds(float target) {
    int mapSize = MapGenerator.MAP_SIZE;
    return !(target < 0 || target > mapSize - 1);
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
    currentFrame.setScale(spriteScale);
    currentFrame.setPosition(
        position.x - currentFrame.getWidth() * spriteScale / 2,
        position.y - currentFrame.getHeight() * spriteScale / 2);
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
    if (health <= 0 && !fleeing) {
      fleeing = true;
      onDispersed.accept(this);
    }
  }

  public int getDispersalScore() {
    return despawnOnDefeat ? 50 : 10;
  }

}
