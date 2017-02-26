package com.bsk.sampleapp;

import android.content.Context;
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
    Context context = getApplicationContext();
    return new FloatingBubbleConfig.Builder()
        .bubbleIcon(ContextCompat.getDrawable(context, R.drawable.web_icon))
        .removeBubbleIcon(ContextCompat.getDrawable(context, com.bsk.floatingbubblelib.R.drawable.close_default_icon))
        .bubbleIconDp(54)
        .expandableView(getInflater().inflate(R.layout.sample_view_1, null))
        .removeBubbleIconDp(54)
        .paddingDp(4)
        .borderRadiusDp(0)
        .physicsEnabled(true)
        .expandableColor(Color.WHITE)
        .triangleColor(0xFF215A64)
        .gravity(Gravity.LEFT)
        .build();
  }
}
