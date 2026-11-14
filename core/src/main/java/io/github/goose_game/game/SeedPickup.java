package io.github.goose_game.game;

/** A seed refill item, dropped by fleeing geese. */
public class SeedPickup extends Pickup {

  /** Constructs a new {@code SeedPickup} instance.
   *
   * @param x the item's x position
   * @param y the item's y position
   * @param width the width of the item
   * @param height the height of the item
   * @param player the player that can pick the item up
  */
  public SeedPickup(float x, float y, float width, float height, Player player) {
    super(x, y, width, height, player, "seeds_bag.png");
  }

  @Override
  protected boolean collect() {
    SeedWeapon seedWeapon = (SeedWeapon) player.get_child("seed_weapon");
    return seedWeapon.addAmmo(1);
  }
}
