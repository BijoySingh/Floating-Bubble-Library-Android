package com.bsk.floatingbubblelib;

/**
 * Floating bubble remove listener
 * Created by bijoysingh on 2/19/17.
 */

public interface FloatingBubbleTouchListener {

  void onTap(boolean expanded);

  void onRemove();

  void onMove(float x, float y);

  void onRelease(float x, float y);

}
