package io.github.goose_game.mapgen;

import java.util.EnumMap;
import java.util.Map;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.utils.Disposable;

public class TileMapRenderer implements Disposable {
  
  private static final Map<TileType, Color> COLOURS = new EnumMap<>(Map.of(
      TileType.GRASS, Color.GREEN,
      TileType.WATER, Color.BLUE,
      TileType.BUILDING, Color.GRAY,
      TileType.BUSH, Color.FOREST,
      TileType.SPAWN, Color.GOLD,
      TileType.ESCAPE, Color.RED
  ));

  private final TileMap map;
  private final Texture pixel;

  public TileMapRenderer(TileMap map) {
    this.map = map;

    Pixmap pm = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
    pm.setColor(Color.WHITE);
    pm.fill();
    pixel = new Texture(pm);
    pm.dispose();
  }

  public void render(Batch batch, OrthographicCamera camera) {
    
    for (int x = 0; x < map.getMapSizeInSquares(); x++) {
      for (int y = 0; y < map.getMapSizeInSquares(); y++) {
        TileType currentTile = map.getTileAtSquare(x, y);
        batch.setColor(COLOURS.get(currentTile));
        batch.draw(pixel, x * MapGenerator.TILE_SIZE, y * MapGenerator.TILE_SIZE, MapGenerator.TILE_SIZE, MapGenerator.TILE_SIZE);
      }
    }
    
    batch.setColor(Color.WHITE);
  }

  @Override 
  public void dispose() {
    pixel.dispose();
  }

}
