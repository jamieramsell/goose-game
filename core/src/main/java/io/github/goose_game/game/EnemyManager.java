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
  
    do { // Find a random spawn point near the player until one is found where geese can spawn
      // Seeded random ensures geese spawn in the same place every time on the same seed
      int rndDir = rng.nextInt(360);
      int rndDist = rng.nextInt(360, 370);

      Vector2 randomRotatedVector = new Vector2(1, 0).rotateDeg(rndDir);
      randomRotatedVector.scl(rndDist);

      targetX = player.position.x + randomRotatedVector.x;
      targetY = player.position.y + randomRotatedVector.y;

      int mapSize = MapGenerator.MAP_SIZE;
      if (targetX < 0 || targetY < 0 || targetX > mapSize || targetY > mapSize) {
        continue; // Goose is out of bounds, try again
      }

    } while (map.isPixelSolidForGeese((int) targetX, (int) targetY));

    Enemy enemy = new Enemy(targetX, targetY, 20, 20, atlas, player, map, 20, 20, 270, 100);
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
