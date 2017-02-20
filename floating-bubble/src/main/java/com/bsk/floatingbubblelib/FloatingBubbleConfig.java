package com.bsk.floatingbubblelib;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.View;
import android.support.v4.content.ContextCompat;
import android.widget.TextView;

/**
 * Floating configurations
 * Created by bijoysingh on 2/19/17.
 */

public class FloatingBubbleConfig {
  private Drawable bubbleIcon;
  private Drawable removeBubbleIcon;
  private View expandableView;
  private int bubbleIconDp;
  private int removeBubbleIconDp;
  private int expandableColor;
  private int gravity;

  private FloatingBubbleConfig(Builder builder) {
    gravity = builder.gravity;
    expandableColor = builder.expandableColor;
    removeBubbleIconDp = builder.removeBubbleIconDp;
    bubbleIconDp = builder.bubbleIconDp;
    expandableView = builder.expandableView;
    removeBubbleIcon = builder.removeBubbleIcon;
    bubbleIcon = builder.bubbleIcon;
  }

  public static FloatingBubbleConfig getDefault(Context context) {
    TextView text = new TextView(context);
    text.setText("HELLO!!!");

    return new Builder()
        .bubbleIcon(ContextCompat.getDrawable(context, R.drawable.bubble_default_icon))
        .removeBubbleIcon(ContextCompat.getDrawable(context, R.drawable.close_default_icon))
        .bubbleIconDp(64)
        .removeBubbleIconDp(64)
        .expandableView(text)
        .expandableColor(Color.WHITE)
        .gravity(Gravity.START)
        .build();
  }

  public Drawable getBubbleIcon() {
    return bubbleIcon;
  }

  public Drawable getRemoveBubbleIcon() {
    return removeBubbleIcon;
  }

  public View getExpandableView() {
    return expandableView;
  }

  public int getBubbleIconDp() {
    return bubbleIconDp;
  }

  public int getRemoveBubbleIconDp() {
    return removeBubbleIconDp;
  }

  public int getExpandableColor() {
    return expandableColor;
  }

  public int getGravity() {
    return gravity;
  }

  public static final class Builder {
    private Drawable bubbleIcon;
    private Drawable removeBubbleIcon;
    private View expandableView;
    private int bubbleIconDp;
    private int removeBubbleIconDp;
    private int expandableColor;
    private int gravity;

    public Builder() {
    }

    public Builder bubbleIcon(Drawable val) {
      bubbleIcon = val;
      return this;
    }

    public Builder removeBubbleIcon(Drawable val) {
      removeBubbleIcon = val;
      return this;
    }

    public Builder expandableView(View val) {
      expandableView = val;
      return this;
    }

    public Builder bubbleIconDp(int val) {
      bubbleIconDp = val;
      return this;
    }

    public Builder removeBubbleIconDp(int val) {
      removeBubbleIconDp = val;
      return this;
    }

    public Builder expandableColor(int val) {
      expandableColor = val;
      return this;
    }

    public FloatingBubbleConfig build() {
      return new FloatingBubbleConfig(this);
    }

    public Builder gravity(int val) {
      gravity = val;
      return this;
    }
  }


}
