package io.github.goose_game.game;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import java.util.HashMap;
import java.util.Map;

public class Node {
  protected final Rectangle bounds;
  protected Texture texture;
  public Vector2 position = new Vector2();
  public int zIndex = 0;
  private Node parent = null;
  private final Map<String, Node> children = new HashMap<>();


  public Node(float x, float y, float width, float height) {
    this.bounds = new Rectangle(x, y, width, height);
    this.texture = null;
  }

  public Node(float x, float y, float width, float height, Texture texture) {
    this.bounds = new Rectangle(x, y, width, height);
    this.texture = texture;
  }

  public void draw(Batch batch) {
    for (Node child: children.values()) {
      child.draw(batch);
    }
    if (texture == null) return;

    batch.draw(texture, position.x, position.y, bounds.width, bounds.height);
  }

  public void update(float delta) {
    for (Node child: children.values()) {
      child.update(delta);
    }
  }

  public Node getParent() {
    return parent;
  }

  public void add_child(String id, Node entity) {
    entity.parent = this;
    children.put(id, entity);
  }

  public Node get_child(String id) {
    return children.get(id);
  }
}
