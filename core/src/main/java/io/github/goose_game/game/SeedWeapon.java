package io.github.goose_game.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import io.github.goose_game.utils.Node;

/**
 * The player's ranged weapon, which throws {@link SeedProjectile} instances towards the mouse
 * cursor.
 *
 * <p>The weapon has limited ammo, refilled by {@link SeedPickup}, and a cooldown between throws.
 * Projectiles are added as children of this node, and it must be a child of the {@link Player}.
 */
public class SeedWeapon extends Node {
  private static final float ATTACK_TIME = 60f;
  private static final int MAX_AMMO = 5;

  private final Sprite seedSprite;
  private final float attackTime = ATTACK_TIME;
  private float attackCounter;
  private int projectileCounter;
  private int ammo = MAX_AMMO;

  /**
   * Creates a seed weapon with full ammo that is ready to fire.
   *
   * @param seedSprite the sprite copied for each thrown seed
   */
  public SeedWeapon(Sprite seedSprite) {
    this.bounds.set(0, 0, 0, 0);
    this.seedSprite = seedSprite;
    this.attackCounter = attackTime;
  }

  /**
   * Throws a seed from the player's position towards the mouse cursor.
   *
   * <p>Does nothing if there is no ammo, the weapon is still on cooldown, or the cursor is exactly
   * on the player.
   */
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

  /** @return the number of seeds remaining */
  public int getAmmo() {
    return ammo;
  }

  /**
   * Adds seeds to the weapon, up to the maximum capacity.
   *
   * @param amount the number of seeds to add
   * @return true if any ammo could be added, or false if the weapon was already full
   */
  public boolean addAmmo(int amount) {
    if (ammo < MAX_AMMO) {
      ammo = Math.min(ammo + amount, MAX_AMMO);
      return true;
    }
    return false;
  }
}
