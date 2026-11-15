package io.github.goose_game.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
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

/**
 * A goose that chases the player and damages them on contact.
 *
 * <p>An enemy is drawn to the nearest seed projectile and stops to eat it. When its health reaches
 * zero it is "dispersed": it flees away from the player and awards points. Once it is far enough
 * away it either despawns, or respawns near the player with full health.
 */
public class Enemy extends Node {
  /** How long, in seconds, an enemy spends eating a seed before the seed is consumed. */
  private static final float SEED_EAT_DURATION = 1.25f;

  private final TileMap map;
  Player target;

  private int health;
  private int maxHealth;
  float speed;
  Vector2 target_pos;
  Vector2 velocity = new Vector2(0, 0);
  private final boolean angry;

  private Array<Animation<Sprite>> animations;
  private float frame;

  float damage;
  int attackCounter;
  int attackDelay;
  boolean fleeing = false;
  private int i = 0;
  private boolean facingRight = true;
  private SeedProjectile eatingSeed;
  private float seedEatTimeRemaining;
  private final float spriteScale;
  private final boolean despawnOnDefeat;
  private final Consumer<Enemy> onDispersed;

  /**
   * Creates an enemy.
   *
   * @param x starting x position
   * @param y starting y position
   * @param width width of the enemy's bounds
   * @param height height of the enemy's bounds
   * @param atlas atlas containing the goose walk and attack animations
   * @param target the player the enemy chases
   * @param map the map used for wall collision
   * @param speed movement acceleration applied each frame
   * @param damage damage dealt to the player per attack
   * @param angry whether the goose is one of the 'angry' goose variant
   * @param attackDelay number of frames between attacks
   * @param health hit points before the enemy is dispersed
   * @param spriteScale scale applied to the sprite when drawn
   * @param despawnOnDefeat whether the enemy is removed, rather than respawned, after fleeing
   * @param onDispersed callback invoked when the enemy is dispersed
   */
  public Enemy(
      float x,
      float y,
      float width,
      float height,
      TextureAtlas atlas,
      Player target,
      TileMap map,
      float speed,
      float damage,
      boolean angry,
      int attackDelay,
      int health,
      float spriteScale,
      boolean despawnOnDefeat,
      Consumer<Enemy> onDispersed
  ) {
    super(x, y, width, height);
    this.target = target;
    this.speed = speed;
    this.damage = damage;
    this.attackCounter = 0;
    this.attackDelay = attackDelay;
    this.target_pos = target.position;
    this.health = health;
    this.angry = angry;
    this.maxHealth = health;
    this.spriteScale = spriteScale;
    this.despawnOnDefeat = despawnOnDefeat;
    this.onDispersed = onDispersed;
    this.animations = new Array<>();
    this.map = map;

    // Animations 0-1 are walking (left, right); 2-3 are attacking (left, right).
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
    // While eating, the enemy stands still and skips all other behaviour
    if (eatingSeed != null) {
      // Another enemy finished the seed first
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

    // Fleeing enemies ignore seeds
    SeedProjectile seedTarget = fleeing ? null : enemyManager.getClosestSeed(position);
    boolean attractedToSeed = seedTarget != null;

    // Attack cooldown
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

    // Head for the nearest seed if there is one, otherwise the player
    Vector2 movementTarget = attractedToSeed ? seedTarget.position : target.position;
    if (attractedToSeed) {
      updateFacing(movementTarget.x - position.x);
    }
    Vector2 dir = movementTarget.cpy().sub(position).nor();

    if (fleeing) { // Run directly away from the player, then de/respawn
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

    // Start eating the seed once close enough
    if (attractedToSeed && position.dst2(seedTarget.position) <= 12 * 12) {
      eatingSeed = seedTarget;
      seedEatTimeRemaining = SEED_EAT_DURATION;
      velocity.setZero();
      super.update(delta);
      return;
    }

    // Damp the previous velocity, then accelerate towards the movement target.
    velocity.x *= 0.65F;
    velocity.y *= 0.65F;

    velocity.x += dir.x * speed;
    velocity.y += dir.y * speed;

    // Push away from other enemies that are too close so they spread out rather than stacking.
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

    // Stop on top of the player rather than jittering through them
    if (overlaps(target) && !fleeing && !attractedToSeed) {
      velocity = new Vector2(0, 0);
    }

    // Fleeing enemies run faster
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

  /**
   * Checks that a coordinate lies within the map.
   *
   * @param target the x or y coordinate to check
   * @return true if the coordinate is inside the map
   */
  private boolean isTargetPosInBounds(float target) {
    int mapSize = MapGenerator.MAP_SIZE;
    return !(target < 0 || target > mapSize - 1);
  }

  @Override
  public void draw(Batch batch) {
    frame += 2 * Gdx.graphics.getDeltaTime();

    // Face the seed being chased, otherwise face the player
    SeedProjectile seedTarget =
        fleeing ? null : ((EnemyManager) getParent()).getClosestSeed(position);
    if (seedTarget != null) {
      updateFacing(seedTarget.position.x - position.x);
    } else if (target.position.x != position.x) {
      facingRight = target.position.x > position.x;
    }

    // Pick the correct animation
    i = facingRight ? 1 : 0;
    if (!fleeing && attackCounter != 0) {
      i += 2;
    }

    // Fleeing enemies move away from the player, so their walk animation is mirrored
    if (fleeing) {
      i = facingRight ? 0 : 1;
    }

    Sprite currentFrame = animations.get(i).getKeyFrame(frame);
    currentFrame.setScale(spriteScale);
    currentFrame.setPosition(
        position.x - currentFrame.getWidth() * spriteScale / 2,
        position.y - currentFrame.getHeight() * spriteScale / 2);

    currentFrame.setColor(angry ? Color.RED : Color.WHITE);
    currentFrame.draw(batch);

    for (Node child : children.values()) {
      child.draw(batch);
    }
  }

  /**
   * Updates which way the enemy faces. A direction of 0 leaves the facing unchanged.
   *
   * @param horizontalDirection positive to face right, negative to face left
   */
  private void updateFacing(float horizontalDirection) {
    if (horizontalDirection != 0) {
      facingRight = horizontalDirection > 0;
    }
  }

  /**
   * Applies damage to the enemy. When health reaches zero the enemy starts fleeing, drops a pickup
   * and notifies the dispersal callback. Further damage while fleeing has no extra effect.
   *
   * @param damage the amount of health to remove
   */
  public void damage(int damage) {
    health -= damage;
    if (health <= 0 && !fleeing) {
      fleeing = true;
      ((EnemyManager) getParent()).queuePickup(position);
      onDispersed.accept(this);
    }
  }

  /** @return the points awarded for dispersing this enemy */
  public int getDispersalScore() {
    int score = 10;
    if (angry) { // Angry goose XP boost
      score *= 2;
    }
    if (spriteScale > 1) { // Large goose XP boost
      score *= 5;
    }

    return score;
  }

}
