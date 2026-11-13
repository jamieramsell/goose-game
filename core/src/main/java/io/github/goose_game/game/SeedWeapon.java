package io.github.goose_game.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import io.github.goose_game.utils.Node;

import java.util.Random;

public class SeedWeapon extends Node {
  private float attackTime;
  private float attackCounter;
  private int projectileCounter;
  private Vector2 dv = new Vector2();
  private float attackAngle;

  public SeedWeapon() {
    this.bounds.set(0, 0, 0, 0);
  }

  public void attack() {
    if (attackCounter >= attackTime) {
      projectileCounter++;
      attackCounter = 0;
      Vector3 temp =
        ((Player) getParent())
          .getCamera()
          .unproject(new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0));
      Vector2 mousePosition = new Vector2(temp.x, temp.y);
      Sprite seedSprite = new Sprite(new Texture(Gdx.files.internal("seeds.png")));
      seedSprite.setRotation(MathUtils.random(0F, 360F));
      System.out.println(seedSprite.getRotation());
      add_child(String.valueOf(projectileCounter), new SeedProjectile(mousePosition, seedSprite));
      get_child(String.valueOf(projectileCounter)).position.set(getParent().position.x, getParent().position.y);

    }
  }

  public void update(float deltaTime) {
    super.update(deltaTime);
    if (attackCounter < attackTime) {
      attackCounter += 240 * deltaTime;
    }
  }
}
