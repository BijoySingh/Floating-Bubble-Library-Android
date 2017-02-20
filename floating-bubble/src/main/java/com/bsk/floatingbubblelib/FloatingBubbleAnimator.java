package com.bsk.floatingbubblelib;

import android.animation.ValueAnimator;
import android.view.View;
import android.view.WindowManager;

/**
 * Animator
 * Created by bijoysingh on 2/19/17.
 */

public class FloatingBubbleAnimator {

  private static final int ANIMATION_TIME = 100;
  private static final int ANIMATION_STEPS = 5;

  public static void animate(
      final View bubbleView,
      final WindowManager windowManager,
      final float x,
      final float y) {
    final WindowManager.LayoutParams bubbleParams =
        (WindowManager.LayoutParams) bubbleView.getLayoutParams();
    final float startX = bubbleParams.x;
    final float startY = bubbleParams.y;
    ValueAnimator animator = ValueAnimator.ofInt(0, 5)
        .setDuration(ANIMATION_TIME);
    animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
      @Override
      public void onAnimationUpdate(ValueAnimator valueAnimator) {
        float currentX = startX + ((x - startX) *
            (Integer) valueAnimator.getAnimatedValue() / ANIMATION_STEPS);
        float currentY = startY + ((y - startY) *
            (Integer) valueAnimator.getAnimatedValue() / ANIMATION_STEPS);
        bubbleParams.x = (int) currentX;
        bubbleParams.y = (int) currentY;
        windowManager.updateViewLayout(bubbleView, bubbleParams);
      }
    });
    animator.start();
  }
}
