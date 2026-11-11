package io.github.goose_game.game;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import io.github.goose_game.utils.Node;

public class Effect extends Node {
  private final Animation<Sprite> animation;
  private float stateTime;

  public Effect(
      float x,
      float y,
      TextureAtlas atlas,
      String regionName,
      int framesPerAnimation,
      float frameDuration) {
    super(x, y, 0, 0);
    if (framesPerAnimation <= 0 || frameDuration <= 0) {
      throw new IllegalArgumentException("Frame count and frame duration must be positive");
    }

    Array<Sprite> atlasFrames = atlas.createSprites(regionName);
    int animationCount = atlasFrames.size / framesPerAnimation;
    if (animationCount == 0) {
      throw new IllegalArgumentException(
          "Effect animation must have at least one complete frame set");
    }

    int startFrame = MathUtils.random(animationCount - 1) * framesPerAnimation;
    Array<Sprite> frames = new Array<>(framesPerAnimation);
    for (int i = startFrame; i < startFrame + framesPerAnimation; i++) {
      frames.add(atlasFrames.get(i));
    }
    Sprite firstFrame = frames.first();
    super.bounds.set(x, y, firstFrame.getWidth(), firstFrame.getHeight());
    this.position.set(x, y);
    animation = new Animation<>(frameDuration, frames, Animation.PlayMode.NORMAL);
  }

  @Override
  public void update(float delta) {
    stateTime += delta;
    if (animation.isAnimationFinished(stateTime)) {
      delete_self();
      return;
    }
    super.update(delta);
  }

  @Override
  public void draw(Batch batch) {
    Sprite frame = animation.getKeyFrame(stateTime);
    frame.setPosition(position.x - frame.getWidth() / 2, position.y - frame.getHeight() / 2);
    frame.draw(batch);
    super.draw(batch);
  }
}
