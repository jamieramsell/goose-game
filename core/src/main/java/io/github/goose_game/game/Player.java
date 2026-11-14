package io.github.goose_game.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

import io.github.goose_game.mapgen.MapGenerator;
import io.github.goose_game.mapgen.TileMap;
import io.github.goose_game.utils.Node;

public class Player extends Node {
  private final TileMap map;
  private final float speed;
  private final float maxHealth;

  private Vector2 velocity;
  private float health;
  private final float MAX_HEALTH;

  private Array<Animation<Sprite>> running;
  private Array<Animation<Sprite>> idle;
  private float frame;
  int i = 0;
  Camera camera;

  public Player(
      float x,
      float y,
      float width,
      float height,
      TextureAtlas atlas,
      TileMap map,
      float speed,
      float health,
      Camera camera) {
    super(x, y, width, height);
    this.speed = speed;
    this.maxHealth = health;
    this.health = health;
    this.MAX_HEALTH = health;
    this.velocity = new Vector2();
    this.camera = camera;
    this.map = map;

    this.running = new Array<>();
    this.idle = new Array<>();
    String[] directions = {"Left", "Right", "Back", "Front"};

    for (int i = 0; i < 4; i++) {
      this.idle.add(
          new Animation<>(
              0.16f, atlas.createSprites(directions[i] + "Idle"), Animation.PlayMode.LOOP));
      this.running.add(
          new Animation<>(
              0.16f, atlas.createSprites(directions[i] + "Run"), Animation.PlayMode.LOOP));
    }
  }

  @Override
  public void update(float delta) {
    Vector2 direction = get_input();
    velocity.x += direction.x * speed;
    velocity.y += direction.y * speed;

    velocity.scl(0.65f);

    float targetX = position.x + velocity.x * delta;
    float targetY = position.y + velocity.y * delta;

    /*
     * Validate that each direction is within bounds.
     *
     * Dev note: this is done seperately so that, for example, if the player holds D and S when
     * there is a wall on their right hand side, they still move downwards, rather than not at all.
     *
     * When a player collides with a wall, reset velocity in that direction to prevent a 'sticky
     * wall' effect, where the player cannot move as their velocity is still positive in the
     * direction of the wall.
     */
    if (isTargetPosInBounds(targetX) && !map.isPixelSolidForPlayer((int) targetX, (int) position.y)) {
      position.x = targetX;
    } else {
      velocity.x = 0;
    }

    if (isTargetPosInBounds(targetY) && !map.isPixelSolidForPlayer((int) position.x, (int) targetY)) {
      position.y = targetY;
    } else {
      velocity.y = 0;
    }

    super.update(delta);
  }

  private boolean isTargetPosInBounds(float target) {
    int mapSize = MapGenerator.MAP_SIZE;
    return !(target < 0 || target > mapSize - 1);
  }

  private Vector2 get_input() {
    Vector2 dir = new Vector2();
    if (Gdx.input.isKeyPressed(Input.Keys.A)) {
      dir.x -= 1;
    }
    if (Gdx.input.isKeyPressed(Input.Keys.D)) {
      dir.x += 1;
    }
    if (Gdx.input.isKeyPressed(Input.Keys.S)) {
      dir.y -= 1;
    }
    if (Gdx.input.isKeyPressed(Input.Keys.W)) {
      dir.y += 1;
    }
    if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
      ((Weapon) get_child("weapon_test")).attack();
    }
    if (Gdx.input.isButtonJustPressed(Input.Buttons.RIGHT)) {
      ((SeedWeapon) get_child("seed_weapon")).attack();
    }
    dir = dir.nor();

    return dir;
  }

  @Override
  public void draw(Batch batch) {
    for (Node child : children.values()) {
      child.draw(batch);
    }

    frame += 2 * Gdx.graphics.getDeltaTime();
    if (velocity.x > 0.02f) {
      i = 1;
    } else if (velocity.x < -0.02f) {
      i = 0;
    } else if (velocity.y > 0.02f) {
      i = 2;
    } else if (velocity.y < -0.02f) {
      i = 3;
    }

    if (velocity.len() <= 0.02f) {
      Sprite currentFrame = idle.get(i).getKeyFrame(frame);
      currentFrame.setPosition(
          position.x - currentFrame.getWidth() / 2, position.y - currentFrame.getHeight() / 2);
      currentFrame.draw(batch);
    } else {
      Sprite currentFrame = running.get(i).getKeyFrame(frame);
      currentFrame.setPosition(
          position.x - currentFrame.getWidth() / 2, position.y - currentFrame.getHeight() / 2);
      currentFrame.draw(batch);
    }
  }

  public void damage(float damage) {
    health = Math.max(0, health - damage);
  }

  public float getHealth() {
    return health;
  }

  public float getMaxHealth() {
    return maxHealth;
  }

  public Camera getCamera() {
    return camera;
  }

  public boolean heal() {
    if (health < MAX_HEALTH) {
      health = Math.min(health + MAX_HEALTH / 10f, MAX_HEALTH);
      return true;
    }
    return false;
  }

}
