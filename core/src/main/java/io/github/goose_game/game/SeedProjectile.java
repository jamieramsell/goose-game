package io.github.goose_game.game;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import io.github.goose_game.utils.Node;

public class SeedProjectile extends Node {
  private static final float SPEED = 300f;
  private final Vector2 targetPosition;
  private boolean landed;
  private boolean consumed;

  public SeedProjectile(Vector2 targetPosition, Sprite texture) {
    super(0, 0, 16, 16, texture);
    this.targetPosition = targetPosition.cpy();
  }

  @Override
  public void update(float deltaTime) {
    if (!landed) {
      Vector2 displacement = targetPosition.cpy().sub(position);
      float distance = displacement.len();
      float travelDistance = SPEED * deltaTime;

      if (distance <= travelDistance || distance == 0) {
        position.set(targetPosition);
        landed = true;
      } else {
        position.mulAdd(displacement, travelDistance / distance);
      }
    }
    super.update(deltaTime);
  }

  public boolean isLanded() {
    return landed;
  }

  public boolean isConsumed() {
    return consumed;
  }

  public void consume() {
    if (!consumed) {
      consumed = true;
      delete_self();
    }
  }
}
