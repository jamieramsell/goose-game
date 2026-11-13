package io.github.goose_game.game;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import io.github.goose_game.utils.Node;
import java.util.ArrayList;
import java.util.function.Consumer;

public class EnemyManager extends Node {

  private final TextureAtlas atlas;
  private final Player player;

  int wave_timer;
  int waves = 0;

  private final Consumer<Enemy> onEnemyDispersed;
  int enemyCounter = 0;

  public EnemyManager(TextureAtlas atlas, Player player, Consumer<Enemy> onEnemyDispersed) {
    this.atlas = atlas;
    this.player = player;
    this.onEnemyDispersed = onEnemyDispersed;
    spawnWave();
  }

  public void spawnWave() {
    int count = 0;
    while (count != waves + 10) {
      spawnEnemy();
      count += 1;
    }
  }

  public void spawnEnemy() {
    // Positional values are assigned by addEnemyNearPlayer(), these are temporary
    float x = 1000;
    float y = 1000;

    float gooseWidth = 20;
    float gooseHeight = 20;
    float spriteScale = 2;

    float speed = 20;
    float damage = 20;
    int attackDelay = 20;
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
