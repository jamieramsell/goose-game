package io.github.goose_game;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class Entity {
  protected final Rectangle bounds;
  protected Texture texture;
  public Vector2 position = new Vector2();


  public Entity(float x, float y, float width, float height) {
    this.bounds = new Rectangle(x, y, width, height);
    this.texture = null;
  }

  public Entity(float x, float y, float width, float height, Texture texture) {
    this.bounds = new Rectangle(x, y, width, height);
    this.texture = texture;
  }

  public void draw(Batch batch) {
    if (texture == null) return;

    batch.draw(texture, position.x, position.y, bounds.width, bounds.height);
  }

  public void update(float delta) {

  }
}
