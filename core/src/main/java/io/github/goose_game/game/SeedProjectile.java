package io.github.goose_game.game;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import io.github.goose_game.utils.Node;

/**
 * A seed thrown by the player's {@link SeedWeapon}.
 *
 * <p>The seed travels in a straight line to its target position and then lands. Landed seeds can be
 * targeted and eaten by {@link Enemy} instances, which removes the seed via {@link #consume()}.
 */
public class SeedProjectile extends Node {
  private static final float SPEED = 300f;
  private final Vector2 targetPosition;
  private boolean landed;
  private boolean consumed;

  /**
   * Creates a seed that will fly towards a target position.
   *
   * @param targetPosition where the seed lands; copied, so later changes to it are not reflected
   * @param texture the sprite used to draw the seed
   */
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

  /** @return whether the seed has reached its target position */
  public boolean isLanded() {
    return landed;
  }

  /** @return whether the seed has been eaten and removed */
  public boolean isConsumed() {
    return consumed;
  }

  /** Marks the seed as eaten and removes it from the scene. Does nothing if already consumed. */
  public void consume() {
    if (!consumed) {
      consumed = true;
      delete_self();
    }
  }
}
