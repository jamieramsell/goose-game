package io.github.goose_game.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import io.github.goose_game.utils.Node;

public class SeedWeapon extends Node {
  private static final float ATTACK_TIME = 60f;
  private static final int MAX_AMMO = 5;

  private final Sprite seedSprite;
  private final float attackTime = ATTACK_TIME;
  private float attackCounter;
  private int projectileCounter;
  private int ammo = MAX_AMMO;

  public SeedWeapon(Sprite seedSprite) {
    this.bounds.set(0, 0, 0, 0);
    this.seedSprite = seedSprite;
    this.attackCounter = attackTime;
  }

  public void attack() {
    if (ammo > 0 && attackCounter >= attackTime) {
      Player player = (Player) getParent();
      Vector3 mouse =
          player
              .getCamera()
              .unproject(new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0));
      Vector2 targetPosition = new Vector2(mouse.x, mouse.y);
      if (targetPosition.dst2(player.position) == 0) {
        return;
      }

      projectileCounter++;
      attackCounter = 0;
      ammo--;
      SeedProjectile seed = new SeedProjectile(targetPosition, new Sprite(seedSprite));
      seed.position.set(player.position);
      add_child(String.valueOf(projectileCounter), seed);
    }
  }

  @Override
  public void update(float deltaTime) {
    if (attackCounter < attackTime) {
      attackCounter += 240 * deltaTime;
      attackCounter = Math.min(attackCounter, attackTime);
    }
    super.update(deltaTime);
  }

  public int getAmmo() {
    return ammo;
  }
}
