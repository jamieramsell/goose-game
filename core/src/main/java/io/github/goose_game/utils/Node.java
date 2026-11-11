package io.github.goose_game.utils;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Node {
  protected Rectangle bounds;
  protected Sprite texture;
  public Vector2 position = new Vector2();
  public int zIndex = 0;
  private Node parent = null;
  protected Map<String, Node> children = new HashMap<>();
  public String key = "";

  public Node(float x, float y, float width, float height) {
    this.bounds = new Rectangle(x, y, width, height);
    this.position.set(x, y);
    this.texture = null;
  }

  public Node(float x, float y, float width, float height, Sprite texture) {
    this.bounds = new Rectangle(x, y, width, height);
    this.position.set(x, y);
    this.texture = texture;
  }

  public Node() {
    this.bounds = new Rectangle(0, 0, 0, 0);
    this.position.set(0, 0);
    this.texture = null;
  }

  public void draw(Batch batch) {
    for (Node child : children.values()) {
      child.draw(batch);
    }
    if (texture == null) return;

    batch.draw(
        texture,
        position.x - texture.getWidth() / 2,
        position.y - texture.getHeight() / 2,
        texture.getWidth(),
        texture.getHeight());
  }

  public void drawDebugBounds(ShapeRenderer shapeRenderer) {
    if (bounds.width > 0 && bounds.height > 0) {
      shapeRenderer.rect(bounds.x, bounds.y, bounds.width, bounds.height);
    }
    for (Node child : children.values()) {
      child.drawDebugBounds(shapeRenderer);
    }
  }

  public void update(float delta) {
    bounds.setCenter(position.x, position.y);
    for (Node child : new ArrayList<>(children.values())) {
      child.update(delta);
    }
  }

  public Node getParent() {
    return parent;
  }

  public boolean overlaps(Node node) {
    if (node == null) return false;
    return this.bounds.overlaps(node.bounds);
  }

  public void add_child(String id, Node entity) {
    entity.parent = this;
    entity.key = id;
    children.put(id, entity);
  }

  public void delete_self() {
    getParent().children.remove(key, this);
  }

  public Node get_child(String id) {
    return children.get(id);
  }

  public Map<String, Node> getChildren() {
    return children;
  }
}
