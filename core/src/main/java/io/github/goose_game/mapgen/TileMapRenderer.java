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
    // Calculate which tiles are in view to avoid drawing ones that are off screen
    float camX = camera.position.x;
    float camY = camera.position.y;

    float halfViewWidth = camera.viewportWidth / 2;
    float halfViewHeight = camera.viewportHeight / 2;

    // These are the pixel coordinates of the view area
    int minX = (int) Math.floor(camX - halfViewWidth);
    int maxX = (int) Math.floor(camX + halfViewWidth);
    int minY = (int) Math.floor(camY - halfViewHeight);
    int maxY = (int) Math.floor(camY + halfViewHeight);

    // Convert the pixel coordinates into tile coordinates; clamp at map edges
    int mapSize = map.getMapSizeInSquares();
    int tileSize = MapGenerator.TILE_SIZE;

    int minTileX = Math.max(0, minX / tileSize);
    int maxTileX = Math.min(mapSize - 1, maxX / tileSize);
    int minTileY = Math.max(0, minY / tileSize);
    int maxTileY = Math.min(mapSize - 1, maxY / tileSize);

    if (minTileX > mapSize - 1 || minTileY > mapSize - 1 || maxTileX < 0 || maxTileY < 0) {
      return; // Camera is entirely off map, so draw nothing. Should never happen in production.
    }

    // Draw all tiles in view
    for (int x = minTileX; x <= maxTileX; x++) {
      for (int y = minTileY; y <= maxTileY; y++) {
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
