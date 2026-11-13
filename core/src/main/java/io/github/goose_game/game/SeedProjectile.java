package io.github.goose_game.game;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import io.github.goose_game.utils.Node;

public class SeedProjectile extends Node {
  private float speed = 300f;
  private Vector2 targetPosition = new Vector2(1, 0);

  public SeedProjectile(Vector2 targetPosition, Sprite texture) {
    super(0, 0, 16, 16, texture);
    this.targetPosition = targetPosition;
  }

  public void update(float deltaTime) {
    super.update(deltaTime);
    Vector2 displacement = targetPosition.cpy().sub(position);
    float distance = displacement.len();
    float travelDistance = speed * deltaTime;

    if (distance <= travelDistance || distance == 0) {
      position.set(targetPosition);
    } else {
      position.mulAdd(displacement, travelDistance / distance);
    }
  }

  public void draw() {

  }
}
