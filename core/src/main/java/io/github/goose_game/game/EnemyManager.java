package io.github.goose_game.game;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector2;
import io.github.goose_game.mapgen.MapGenerator;
import io.github.goose_game.mapgen.TileMap;
import io.github.goose_game.utils.Node;
import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.Random;

/**
 * Node responsible for spawning and tracking {@link Enemy} instances.
 *
 * <p>Enemies are added as children of this node and spawn in a ring around the {@link Player}. A
 * first wave is spawned on construction; further waves and large enemies are triggered externally
 * (e.g. by time based events).
 */
public class EnemyManager extends Node {

  private final TextureAtlas atlas;
  private final Player player;
  private final TileMap map;
  private final Random rng;
  private final ArrayList<Vector2> pendingPickups = new ArrayList<>();

  /** Timer used for wave scheduling. */
  int wave_timer;

  /** Number of waves spawned so far. Each wave adds one extra enemy. */
  int waves = 0;

  /** Callback passed to each spawned enemy, invoked when it is dispersed. */
  private final Consumer<Enemy> onEnemyDispersed;

  /** Running count of enemies spawned used to generate unique child names. */
  int enemyCounter = 0;

  /** Running count of pickups spawned used to generate unique child names. */
  private int PickupCounter;

  /**
   * Creates the manager and immediately spawns the first wave of enemies.
   *
   * @param atlas atlas containing the goose animations
   * @param player the player enemies chase and spawn around
   * @param map the map used for spawn validation and collision; its seed seeds spawn randomness
   * @param onEnemyDispersed callback invoked whenever a spawned enemy is dispersed
   */
  public EnemyManager(
      TextureAtlas atlas,
      Player player,
      TileMap map,
      Consumer<Enemy> onEnemyDispersed
  ) {
    this.atlas = atlas;
    this.player = player;
    this.map = map;
    this.rng = new Random(map.seed);
    this.onEnemyDispersed = onEnemyDispersed;
    spawnWave();
  }

  /** Spawns a wave of standard enemies; the wave size is {@code waves + 10}. */
  public void spawnWave() {
    int count = 0;
    while (count != waves + 10) {
      spawnEnemy();
      count += 1;
    }
  }

  /** Spawns a single standard enemy (small, 100 health) near the player. */
  public void spawnEnemy() {
    spawnEnemy(false);
  }

  /**
   * Spawns a single enemy (small, 100 health) near the player.
   * 
   * @param angry whether or not the goose is 'angry'. Angry geese run slightly faster, and are red 
   *     in colour. They are spawned by the tower defence event.
   */
  public void spawnEnemy(boolean angry) {
    // Positional values are assigned by teleportEnemyNearPlayer(), these are temporary
    float x = 1000;
    float y = 1000;

    float gooseWidth = 20;
    float gooseHeight = 20;
    float spriteScale = 1;

    float speed = angry ? 25 : 20;
    float damage = 20;
    int attackDelay = 270;
    int health = 100;
    boolean despawnOnDefeat = angry; // Despawn if angry, respawn if standard

    Enemy enemy =
        new Enemy(
            x,
            y,
            gooseWidth,
            gooseHeight,
            atlas,
            player,
            map,
            speed,
            damage,
            angry,
            attackDelay,
            health,
            spriteScale,
            despawnOnDefeat,
            onEnemyDispersed);

    teleportEnemyNearPlayer(enemy);

    enemyCounter += 1;
    add_child(String.valueOf(enemyCounter), enemy);
  }

  /**
   * Spawns a single large enemy near the player. Large enemies have more health and damage, a
   * longer attack delay, and despawn when defeated.
   */
  public void spawnLargeEnemy() {
    // Positional values are assigned by teleportEnemyNearPlayer(), these are temporary
    float x = 1000;
    float y = 1000;

    float gooseWidth = 40;
    float gooseHeight = 40;
    float spriteScale = 2;

    float speed = 20;
    float damage = 30;
    int attackDelay = 270;
    int health = 200;
    boolean despawnOnDefeat = true;

    Enemy enemy =
        new Enemy(
            x,
            y,
            gooseWidth,
            gooseHeight,
            atlas,
            player,
            map,
            speed,
            damage,
            false,
            attackDelay,
            health,
            spriteScale,
            despawnOnDefeat,
            onEnemyDispersed);

    teleportEnemyNearPlayer(enemy);

    enemyCounter += 1;
    add_child(String.valueOf(enemyCounter), enemy);
  }

  /** Spawn a wave of angry geese around the player. */
  public void spawnTowerAttackGeese() {
    for (int i = 0; i < 10; i++) {
      spawnEnemy(true);
    }
  }

  /**
   * Positions the enemy at a random angle and a distance of 360-370 units from the player.
   *
   * @param enemy the enemy to position
   */
  public void teleportEnemyNearPlayer(Enemy enemy) {
    float targetX;
    float targetY;

    while (true) { // Find a random spawn point near the player where geese can spawn
      // Seeded random ensures geese spawn in the same place every time on the same seed
      int rndDir = rng.nextInt(360);
      int rndDist = rng.nextInt(360, 370);

      Vector2 randomRotatedVector = new Vector2(1, 0).rotateDeg(rndDir);
      randomRotatedVector.scl(rndDist);

      targetX = player.position.x + randomRotatedVector.x;
      targetY = player.position.y + randomRotatedVector.y;

      int mapSize = MapGenerator.MAP_SIZE;
      if (targetX <= 0 || targetY <= 0 || targetX >= mapSize || targetY >= mapSize) {
        continue;
      }

      if (!map.isPixelSolidForGeese((int) targetX, (int) targetY)) {
        break;
      }
    }

    enemy.position.x = targetX;
    enemy.position.y = targetY;
  }

  /**
   * Finds the seed nearest to a position that enemies can still be attracted to.
   *
   * <p>Only seeds that have landed and have not yet been consumed are considered.
   *
   * @param position the position to measure distance from
   * @return the closest eligible seed, or {@code null} if there is none or the player has no seed
   *     weapon
   */
  public SeedProjectile getClosestSeed(Vector2 position) {
    Node seedWeapon = player.get_child("seed_weapon");
    if (seedWeapon == null) {
      return null;
    }

    SeedProjectile closestSeed = null;
    float closestDistance2 = Float.MAX_VALUE;
    for (Node child : seedWeapon.getChildren().values()) {
      if (child instanceof SeedProjectile) {
        SeedProjectile seed = (SeedProjectile) child;
        if (seed.isLanded() && !seed.isConsumed()) {
          float distance2 = position.dst2(seed.position);
          if (distance2 < closestDistance2) {
            closestSeed = seed;
            closestDistance2 = distance2;
          }
        }
      }
    }
    return closestSeed;
  }

  /**
   * Queues a possible pickup drop at a position. The drop is resolved on the next {@link
   * #update()}, where it has a chance of spawning a heart or seed pickup.
   *
   * @param position the position to drop at
   */
  void queuePickup(Vector2 position) {
    pendingPickups.add(position.cpy());
  }

  @Override
  public void update(float delta) {
    for (Vector2 position : pendingPickups) {
      if (Math.random() < 0.5) {
        if (Math.random() < 0.5) {
          add_child(
              String.valueOf(PickupCounter),
              new HeartPickup(position.x, position.y, 16, 16, player));
        } else {
          add_child(
              String.valueOf(PickupCounter),
              new SeedPickup(position.x, position.y, 16, 16, player));
        }
        PickupCounter++;
      }
      }
    pendingPickups.clear();
    super.update(delta);
  }
}
