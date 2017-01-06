package com.bsk.floatingbubblelib.service;

import android.content.Context;
import android.content.Intent;

import static com.bsk.floatingbubblelib.service.FloatingBubbleServiceBuilder.IntentKeys.BUBBLE_IMAGE_RESOURCE;
import static com.bsk.floatingbubblelib.service.FloatingBubbleServiceBuilder.IntentKeys.DEBUG_MODE;
import static com.bsk.floatingbubblelib.service.FloatingBubbleServiceBuilder.IntentKeys.REMOVE_IMAGE_RESOURCE;

/**
 * Builder class for the floating bubble service
 * Created by bijoy on 1/6/17.
 */

public class FloatingBubbleServiceBuilder {

  public static final Integer DEFAULT_EMPTY_VALUE = 0;

  public enum IntentKeys {
    DEBUG_MODE,
    REMOVE_IMAGE_RESOURCE,
    BUBBLE_IMAGE_RESOURCE,
  }

  private Context context;
  private Boolean isDebugMode = false;
  private Integer removeBubbleIcon = DEFAULT_EMPTY_VALUE;
  private Integer bubbleIcon = DEFAULT_EMPTY_VALUE;

  public FloatingBubbleServiceBuilder(Context context) {
    this.context = context;
  }

  public FloatingBubbleServiceBuilder setDebugMode(boolean isDebugMode) {
    this.isDebugMode = isDebugMode;
    return this;
  }

  public FloatingBubbleServiceBuilder setRemoveBubbleIcon(Integer icon) {
    this.removeBubbleIcon = icon;
    return this;
  }

  public FloatingBubbleServiceBuilder setBubbleIcon(Integer icon) {
    this.bubbleIcon = icon;
    return this;
  }

  public Intent build() {
    Intent intent = new Intent(context, FloatingBubbleService.class);
    intent.putExtra(DEBUG_MODE.name(), isDebugMode);
    intent.putExtra(REMOVE_IMAGE_RESOURCE.name(), removeBubbleIcon);
    intent.putExtra(BUBBLE_IMAGE_RESOURCE.name(), bubbleIcon);
    return intent;
  }
}
