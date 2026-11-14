package io.github.goose_game.game;

/** A health top-up item, dropped by fleeing geese. */
public class HeartPickup extends Pickup {

  /** Constructs a new {@code HeartPickup} instance.
   *
   * @param x the item's x position
   * @param y the item's y position
   * @param width the width of the item
   * @param height the height of the item
   * @param player the player that can pick the item up
  */
  public HeartPickup(float x, float y, float width, float height, Player player) {
    super(x, y, width, height, player, "heart.png");
  }

  @Override
  protected boolean collect() {
    return player.heal();
  }
}
