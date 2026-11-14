package io.github.goose_game.game;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector2;

import io.github.goose_game.mapgen.MapGenerator;
import io.github.goose_game.mapgen.TileMap;
import io.github.goose_game.utils.Node;
import java.util.ArrayList;
import java.util.Random;

public class EnemyManager extends Node {

  private final TextureAtlas atlas;
  private final Player player;
  private final TileMap map;
  private final Random rng;

  int wave_timer;
  int waves = 0;
  int enemyCounter = 0;

  public EnemyManager(TextureAtlas atlas, Player player, TileMap map) {
    this.atlas = atlas;
    this.player = player;
    this.map = map;
    this.rng = new Random(map.seed);
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

    Enemy enemy = new Enemy(targetX, targetY, 20, 20, atlas, player, map, 20, 20, 270, 100);
    enemyCounter += 1;
    add_child(String.valueOf(enemyCounter), enemy);
  }

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
