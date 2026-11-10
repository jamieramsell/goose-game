package io.github.goose_game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;

public class EnemyManager extends Node {
  int wave_timer;
  int waves = 0;
  private final TextureAtlas atlas;
  private final Player player;
  int enemyCounter = 0;

  public EnemyManager(TextureAtlas atlas, Player player) {
    this.atlas = atlas;
    this.player = player;
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
    Enemy enemy = new Enemy(
      1000, 1000,
      20, 20,
      atlas,
      player,
      20,
      20,
      270,
        100
    );
    int rndDir = MathUtils.random(0,360);
    int rndDist = MathUtils.random(360, 370);
    Vector2 randomRotatedVector = new Vector2(1, 0).rotateDeg(rndDir);
    randomRotatedVector.scl(rndDist);
    enemy.position.x = player.position.x + randomRotatedVector.x;
    enemy.position.y = player.position.y + randomRotatedVector.y;

    enemyCounter += 1;
    add_child(String.valueOf(enemyCounter), enemy);
  }
}
