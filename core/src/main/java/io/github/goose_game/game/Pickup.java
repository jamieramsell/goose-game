package io.github.goose_game.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import io.github.goose_game.utils.Node;

public abstract class Pickup extends Node {
  protected final Player player;
  private float elapsed;

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

  protected abstract boolean collect();
}
