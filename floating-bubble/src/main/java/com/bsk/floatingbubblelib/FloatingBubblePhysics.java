package com.bsk.floatingbubblelib;

import android.view.View;
import android.view.WindowManager;

/**
 * FLoating Bubble Physics
 * Created by bijoysingh on 2/19/17.
 */

public class FloatingBubblePhysics extends DefaultFloatingBubbleTouchListener {

  private int sizeX;
  private int sizeY;
  private View bubbleView;
  private WindowManager windowManager;
  private FloatingBubbleConfig config;

  private WindowManager.LayoutParams bubbleParams;

  private FloatingBubblePhysics(Builder builder) {
    sizeX = builder.sizeX;
    sizeY = builder.sizeY;
    bubbleView = builder.bubbleView;
    windowManager = builder.windowManager;
    config = builder.config;

    bubbleParams = (WindowManager.LayoutParams) bubbleView.getLayoutParams();
  }

  @Override
  public void onRelease(float x, float y) {
    if (x < sizeX / 2) {
      FloatingBubbleAnimator.animate(
          bubbleView,
          windowManager,
          0,
          y);
    } else {
      FloatingBubbleAnimator.animate(
          bubbleView,
          windowManager,
          sizeX - bubbleView.getWidth(),
          y);
    }
  }


  public static final class Builder {
    private int sizeX;
    private int sizeY;
    private View bubbleView;
    private WindowManager windowManager;
    private FloatingBubbleConfig config;

    public Builder() {
    }

    public Builder sizeX(int val) {
      sizeX = val;
      return this;
    }

    public Builder sizeY(int val) {
      sizeY = val;
      return this;
    }

    public Builder bubbleView(View val) {
      bubbleView = val;
      return this;
    }

    public Builder windowManager(WindowManager val) {
      windowManager = val;
      return this;
    }

    public Builder config(FloatingBubbleConfig val) {
      config = val;
      return this;
    }

    public FloatingBubblePhysics build() {
      return new FloatingBubblePhysics(this);
    }
  }
}
