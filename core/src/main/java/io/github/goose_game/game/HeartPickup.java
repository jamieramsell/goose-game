package io.github.goose_game.game;

public class HeartPickup extends Pickup {
  public HeartPickup(float x, float y, float width, float height, Player player) {
    super(x, y, width, height, player, "heart.png");
  }

  @Override
  protected boolean collect() {
    return player.heal();
  }
}
