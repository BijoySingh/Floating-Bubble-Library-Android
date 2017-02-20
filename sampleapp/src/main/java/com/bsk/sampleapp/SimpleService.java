package com.bsk.sampleapp;

import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.view.Gravity;
import android.view.WindowManager;

import com.bsk.floatingbubblelib.FloatingBubbleConfig;
import com.bsk.floatingbubblelib.FloatingBubbleService;
import com.bsk.floatingbubblelib.FloatingBubbleTouchListener;

/**
 * Created by bijoysingh on 2/19/17.
 */

public class SimpleService extends FloatingBubbleService {
  @Override
  protected FloatingBubbleConfig getConfig() {
    return new FloatingBubbleConfig.Builder()
        .bubbleIcon(ContextCompat.getDrawable(getApplicationContext(), com.bsk.floatingbubblelib.R.drawable.bubble_default_icon))
        .removeBubbleIcon(ContextCompat.getDrawable(getApplicationContext(), com.bsk.floatingbubblelib.R.drawable.close_default_icon))
        .bubbleIconDp(54)
        .removeBubbleIconDp(54)
        .paddingDp(4)
        .physicsEnabled(true)
        .expandableColor(Color.GRAY)
        .gravity(Gravity.CENTER)
        .build();
  }
}
