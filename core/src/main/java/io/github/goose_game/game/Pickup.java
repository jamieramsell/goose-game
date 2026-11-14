package io.github.goose_game.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import io.github.goose_game.utils.Node;

/** Represents an item which can be picked up by the player. */
public abstract class Pickup extends Node {
  protected final Player player;
  private float elapsed;

  /** Constructs a new {@code Pickup} instance.
   *
   * @param x the item's x position
   * @param y the item's y position
   * @param width the width of the item
   * @param height the height of the item
   * @param player the player that can pick the items up
   * @param texturePath the address of the texture file of the item
  */
  protected Pickup(
      float x, float y, float width, float height, Player player, String texturePath) {
    super(x, y, width, height);
    this.player = player;
    this.texture = new Sprite(new Texture(Gdx.files.internal(texturePath)));
  }

  @Override
  public void update(float deltaTime) {
    elapsed += deltaTime;
    position.y = (float) Math.sin(Gdx.graphics.getFrameId() * 0.01f) * 0.01f + position.y;

    super.update(deltaTime);

    if (overlaps(player) && collect()) {
      delete_self();
    }
  }

  /**
   * The effect triggered by picking up the item.
   * 
   * @return whether the effect was successfully carried out
   */
  protected abstract boolean collect();
}
