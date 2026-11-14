package io.github.goose_game.game;

public class SeedPickup extends Pickup {
  public SeedPickup(float x, float y, float width, float height, Player player) {
    super(x, y, width, height, player, "seeds_bag.png");
  }

  @Override
  protected boolean collect() {
    SeedWeapon seedWeapon = (SeedWeapon) player.get_child("seed_weapon");
    return seedWeapon.addAmmo(1);
  }
}
