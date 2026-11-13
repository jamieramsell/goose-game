package io.github.goose_game.mapgen;

import java.util.EnumMap;
import java.util.Map;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.utils.Disposable;

/**
 * Draws a {@link TileMap} as flat coloured squares, one per tile, as a placeholder until real tile
 * artwork exists.
 *
 * <p>Only the tiles inside the camera's view are drawn each frame, so the cost depends on the size
 * of the window and not on the size of the map.
 *
 * <p>This renderer owns a native {@link Texture}, so {@link #dispose()} must be called when it is
 * no longer needed. The {@link Batch} it draws with belongs to the caller and is not disposed here.
 */
public class TileMapRenderer implements Disposable {

  /** The colour each type of tile is drawn in. */
  private static final Map<TileType, Color> COLOURS = new EnumMap<>(Map.of(
      TileType.GRASS, Color.GREEN,
      TileType.WATER, Color.BLUE,
      TileType.BUILDING, Color.GRAY,
      TileType.BUSH, Color.FOREST,
      TileType.SPAWN, Color.GOLD,
      TileType.ESCAPE, Color.RED
  ));

  /** The map to draw. */
  private final TileMap map;

  /** A 1x1 white texture, which is tinted and stretched to draw each tile as a solid square. */
  private final Texture pixel;

  /**
   * Creates a renderer for the given map.
   *
   * <p>Must be called after libGDX has started, as it creates a texture.
   *
   * @param map the map to draw
   */
  public TileMapRenderer(TileMap map) {
    this.map = map;

    Pixmap pm = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
    pm.setColor(Color.WHITE);
    pm.fill();
    pixel = new Texture(pm);
    pm.dispose();
  }

  /**
   * Draws every tile that is inside the camera's view.
   *
   * <p>Must be called between {@link Batch#begin()} and {@link Batch#end()}, and with the batch's
   * projection matrix already set to the camera's, so that tile positions are interpreted as world
   * coordinates. Call it before drawing entities so that they appear on top of the tiles.
   *
   * <p>The batch's tint colour is changed while drawing, and is reset to white afterwards.
   *
   * @param batch the batch to draw with
   * @param camera the camera whose view determines which tiles are drawn
   */
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

  /** Disposes the texture used to draw tiles. The renderer must not be used afterwards. */
  @Override
  public void dispose() {
    pixel.dispose();
  }

}
