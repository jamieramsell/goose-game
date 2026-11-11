package io.github.goose_game.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import io.github.goose_game.Node;

public class Weapon extends Node {
  private int range;
  private int damage;
  float stateTime = 0;
  Animation<Sprite> animation;
  float attackTime;
  float attackCounter;
  float attackAngle;
  private float knockback;
  private final TextureAtlas atlas;
  private int effectCounter;

  private Vector2 dv = new Vector2();

  public Weapon(float x, float y, float width, float height, Animation<Sprite> texture, TextureAtlas atlas, int range, int damage, float knockback, int attackTime) {
    this.bounds = new Rectangle(x, y, width, height);
    this.position.set(x, y);
    this.animation = texture;
    this.atlas = atlas;
    this.attackTime = attackTime;
    this.attackCounter = attackTime;
    this.range = range;
    this.damage = damage;
    this.knockback = knockback;
  }

  @Override
  public void draw(Batch batch) {
    for (Node child : children.values()) {
      child.draw(batch);
    }
    if (attackCounter != attackTime) {
      int i = (int) (attackCounter / (attackTime / animation.getAnimationDuration()));
      Sprite currentFrame = animation.getKeyFrame(i);
      currentFrame.setPosition(position.x - currentFrame.getWidth() / 2, position.y - currentFrame.getHeight() / 2);
      currentFrame.setRotation(MathUtils.radiansToDegrees * attackAngle);
      currentFrame.draw(batch);
      bounds = currentFrame.getBoundingRectangle();
    }

  }

  public void update(float delta) {
    super.update(delta);

    if (attackTime > attackCounter) {
      attackCounter += 240 * delta;
    }
    else if (attackCounter > attackTime) {
      attackCounter = attackTime;
    }
    Vector2 playerPosition = getParent().position;
    position.set(playerPosition.x + dv.x * 30, playerPosition.y + dv.y * 30);
  }

  public void attack() {
    if (attackTime == attackCounter) {
      attackCounter = 0;
      animation.setPlayMode(Animation.PlayMode.NORMAL);
      Vector3 temp = ((Player) getParent()).getCamera().unproject(new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0));
      Vector2 mousePosition = new Vector2(temp.x, temp.y);
      Vector2 playerPosition = getParent().position;
      dv = mousePosition.cpy().sub(playerPosition).nor();
      attackAngle = MathUtils.atan2(dv.y, dv.x) - 1.5f;
      position.set(playerPosition.x + dv.x * 30, playerPosition.y + dv.y * 30);
      for (Node child : ((EnemyManager) getParent().getParent().get_child("enemyManager")).getChildren().values()) {
        if (this.overlaps(child)) {
          if (child instanceof Enemy) {
            Enemy enemy = (Enemy) child;
            enemy.damage(damage);
            effectCounter += 1;
            enemy.add_child("hit_effect_" + effectCounter,
              new Effect(
                enemy.position.x,
                enemy.position.y,
                atlas,
                "hit_effect",
                4,
                0.06f)
            );
            enemy.velocity.y = -enemy.velocity.y * knockback;
            enemy.velocity.x = -enemy.velocity.x * knockback;
          }
        }
      }
    }
  }
}
