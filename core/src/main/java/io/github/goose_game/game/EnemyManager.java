package io.github.goose_game.game;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import io.github.goose_game.utils.Node;
import java.util.ArrayList;
import java.util.function.Consumer;

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

  /** Timer used for wave scheduling. */
  int wave_timer;
  /** Number of waves spawned so far. Each wave adds one extra enemy. */
  int waves = 0;

  /** Callback passed to each spawned enemy, invoked when it is dispersed. */
  private final Consumer<Enemy> onEnemyDispersed;

  /** Running count of enemies spawned, used to generate unique child names. */
  int enemyCounter = 0;

  /**
   * Creates an enemy manager and immediately spawns the first wave.
   *
   * @param atlas texture atlas used to render enemies
   * @param player the player that enemies target and spawn around
   * @param onEnemyDispersed callback invoked when a spawned enemy is dispersed
   */
  public EnemyManager(TextureAtlas atlas, Player player, Consumer<Enemy> onEnemyDispersed) {
    this.atlas = atlas;
    this.player = player;
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
    // Positional values are assigned by addEnemyNearPlayer(), these are temporary
    float x = 1000;
    float y = 1000;

    float gooseWidth = 20;
    float gooseHeight = 20;
    float spriteScale = 1;

    float speed = 20;
    float damage = 20;
    int attackDelay = 270;
    int health = 100;
    boolean despawnOnDefeat = false;

    Enemy enemy =
        new Enemy(
            x,
            y,
            gooseWidth,
            gooseHeight,
            atlas,
            player,
            speed,
            damage,
            attackDelay,
            health,
            spriteScale,
            despawnOnDefeat,
            onEnemyDispersed);

    addEnemyNearPlayer(enemy);
  }

  /**
   * Spawns a single large enemy near the player. Large enemies have more health and damage, a
   * longer attack delay, and despawn when defeated.
   */
  public void spawnLargeEnemy() {
    // Positional values are assigned by addEnemyNearPlayer(), these are temporary
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
            speed,
            damage,
            attackDelay,
            health,
            spriteScale,
            despawnOnDefeat,
            onEnemyDispersed);

    addEnemyNearPlayer(enemy);
  }

  /**
   * Positions the enemy at a random angle and a distance of 360-370 units from the player, then
   * adds it as a child of this node.
   *
   * @param enemy the enemy to position and register
   */
  private void addEnemyNearPlayer(Enemy enemy) {
    int rndDir = MathUtils.random(0, 360);
    int rndDist = MathUtils.random(360, 370);
    Vector2 randomRotatedVector = new Vector2(1, 0).rotateDeg(rndDir);
    randomRotatedVector.scl(rndDist);
    System.out.println(randomRotatedVector);
    enemy.position.x = player.position.x + randomRotatedVector.x;
    enemy.position.y = player.position.y + randomRotatedVector.y;

    enemyCounter += 1;
    add_child(String.valueOf(enemyCounter), enemy);
  }

  /**
   * Updates all child enemies and collects those that are fleeing.
   *
   * @param delta time elapsed since the last frame, in seconds
   */
  @Override
  public void update(float delta) {
    super.update(delta);
    ArrayList<Enemy> killList = new ArrayList<Enemy>();
    for (Node child : children.values()) {
      if (child instanceof Enemy) {
        if (((Enemy) child).fleeing) {
          killList.add((Enemy) child);
        }
      }
      ;
    }
  }
}
