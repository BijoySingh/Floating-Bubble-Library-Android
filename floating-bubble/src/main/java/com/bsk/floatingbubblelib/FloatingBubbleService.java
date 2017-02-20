package com.bsk.floatingbubblelib;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.bsk.floatingbubblelib.listeners.FloatingBubbleTouchListener;

/**
 * Floating Bubble Service. This file is the actual bubble view.
 * Created by bijoy on 1/6/17.
 */

public class FloatingBubbleService extends Service {

  protected static final String TAG = FloatingBubbleService.class.getSimpleName();

  // Constructor Variable
  protected FloatingBubbleLogger logger;

  // The Window Manager View
  protected WindowManager windowManager;

  // The layout inflater
  protected LayoutInflater inflater;

  // Window Dimensions
  protected Point windowSize = new Point();

  // The Views
  protected View bubbleView;
  protected View removeBubbleView;
  protected View expandableView;

  protected WindowManager.LayoutParams bubbleParams;
  protected WindowManager.LayoutParams removeBubbleParams;
  protected WindowManager.LayoutParams expandableParams;

  private FloatingBubbleConfig config;

  @Override
  public void onCreate() {
    super.onCreate();
    logger = new FloatingBubbleLogger().setDebugEnabled(true).setTag(TAG);
    config = getConfig();
  }

  @Override
  public IBinder onBind(Intent intent) {
    return null;
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    if (intent == null) {
      return Service.START_NOT_STICKY;
    }

    logger.log("Start with START_STICKY");

    // Load the Window Managers
    setupWindowManager();
    setupViews();
    setTouchListener();
    return super.onStartCommand(intent, flags, Service.START_STICKY);

  }


  @Override
  public void onDestroy() {
    super.onDestroy();
    logger.log("onDestroy");
    if (windowManager == null) {
      return;
    }

    if (bubbleView != null) {
      windowManager.removeView(bubbleView);
    }

    if (removeBubbleView != null) {
      windowManager.removeView(removeBubbleView);
    }
  }

  @Override
  public void onConfigurationChanged(Configuration newConfig) {
    super.onConfigurationChanged(newConfig);

  }

  private void setupWindowManager() {
    windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
    inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
    windowManager.getDefaultDisplay().getSize(windowSize);
  }

  /**
   * Creates the views
   */
  protected void setupViews() {
    int padding = dpToPixels(config.getPaddingDp());

    // Setting up view
    bubbleView = inflater.inflate(R.layout.floating_bubble_view, null);
    removeBubbleView = inflater.inflate(R.layout.floating_remove_bubble_view, null);
    expandableView = inflater.inflate(R.layout.floating_expandable_view, null);

    // Setting up the Remove Bubble View setup
    removeBubbleParams = getDefaultWindowParams();
    removeBubbleParams.gravity = Gravity.TOP | Gravity.START;
    removeBubbleParams.width = dpToPixels(config.getRemoveBubbleIconDp());
    removeBubbleParams.height = dpToPixels(config.getRemoveBubbleIconDp());
    removeBubbleParams.x = (windowSize.x - removeBubbleParams.width) / 2;
    removeBubbleParams.y = windowSize.y - removeBubbleParams.height - padding;
    removeBubbleView.setVisibility(View.GONE);
    windowManager.addView(removeBubbleView, removeBubbleParams);

    // Setting up the Expandable View setup
    expandableParams = getDefaultWindowParams(
        WindowManager.LayoutParams.MATCH_PARENT,
        WindowManager.LayoutParams.WRAP_CONTENT);
    expandableParams.gravity = Gravity.TOP | Gravity.START;
    expandableView.setVisibility(View.GONE);
    ((LinearLayout) expandableView).setGravity(config.getGravity());
    expandableView.setPadding(padding, padding, padding, padding);
    windowManager.addView(expandableView, expandableParams);

    // Setting up the Floating Bubble View
    bubbleParams = getDefaultWindowParams();
    bubbleParams.gravity = Gravity.TOP | Gravity.START;
    bubbleParams.width = dpToPixels(config.getBubbleIconDp());
    bubbleParams.height = dpToPixels(config.getBubbleIconDp());
    windowManager.addView(bubbleView, bubbleParams);

    // Setting the configuration
    if (config.getRemoveBubbleIcon() != null) {
      ((ImageView) removeBubbleView).setImageDrawable(config.getRemoveBubbleIcon());
    }
    if (config.getBubbleIcon() != null) {
      ((ImageView) bubbleView).setImageDrawable(config.getBubbleIcon());
    }
    if (config.getExpandableView() != null) {
      ImageView triangle = (ImageView) expandableView.findViewById(R.id.expandableViewTriangle);
      triangle.setColorFilter(config.getExpandableColor());
      ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) triangle.getLayoutParams();
      params.leftMargin = dpToPixels((config.getBubbleIconDp() - 16) / 2);
      params.rightMargin = dpToPixels((config.getBubbleIconDp() - 16) / 2);

      LinearLayout container = (LinearLayout) expandableView.findViewById(R.id.expandableViewContainer);
      container.setBackgroundColor(config.getExpandableColor());
      container.removeAllViews();
      container.addView(config.getExpandableView());
    }
  }

  protected FloatingBubbleConfig getConfig() {
    return FloatingBubbleConfig.getDefault(getApplicationContext());
  }

  protected void setTouchListener() {
    bubbleView.setOnTouchListener(new FloatingBubbleTouch.Builder()
        .sizeX(windowSize.x)
        .sizeY(windowSize.y)
        .logger(logger)
        .listener(new FloatingBubbleTouchListener() {
          @Override
          public void onTap(boolean expanded) {

          }

          @Override
          public void onRemove() {
            stopSelf();
          }

          @Override
          public void onRelease() {

          }
        })
        .bubbleView(bubbleView)
        .removeBubbleSize(dpToPixels(config.getRemoveBubbleIconDp()))
        .windowManager(windowManager)
        .expandableView(expandableView)
        .removeBubbleView(removeBubbleView)
        .config(config)
        .padding(dpToPixels(config.getPaddingDp()))
        .build());
  }

  /**
   * Get the default window layout params
   *
   * @return the layout param
   */
  protected WindowManager.LayoutParams getDefaultWindowParams() {
    return getDefaultWindowParams(
        WindowManager.LayoutParams.WRAP_CONTENT,
        WindowManager.LayoutParams.WRAP_CONTENT);
  }

  /**
   * Get the default window layout params
   *
   * @return the layout param
   */
  protected WindowManager.LayoutParams getDefaultWindowParams(int width, int height) {
    return new WindowManager.LayoutParams(
        width,
        height,
        WindowManager.LayoutParams.TYPE_PHONE,
        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
            | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
            | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
        PixelFormat.TRANSLUCENT);
  }

  /**
   * Converts DPs to Pixel values
   *
   * @return the pixel value
   */
  private int dpToPixels(int dpSize) {
    DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
    return Math.round(dpSize * (displayMetrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT));
  }
}
