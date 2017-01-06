package com.bsk.floatingbubblelib.service;

import android.app.Service;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import com.bsk.floatingbubblelib.R;
import com.bsk.floatingbubblelib.log.FloatingBubbleLogger;

import static com.bsk.floatingbubblelib.service.FloatingBubbleServiceBuilder.DEFAULT_EMPTY_VALUE;
import static com.bsk.floatingbubblelib.service.FloatingBubbleServiceBuilder.IntentKeys.BUBBLE_IMAGE_RESOURCE;
import static com.bsk.floatingbubblelib.service.FloatingBubbleServiceBuilder.IntentKeys.REMOVE_IMAGE_RESOURCE;

/**
 * Floating Bubble Service. This file is the actual bubble view.
 * Created by bijoy on 1/6/17.
 */

public class FloatingBubbleService extends Service {

  private static final String TAG = FloatingBubbleService.class.getSimpleName();

  private static final Integer REMOVE_BOTTOM_MARGIN = 36;
  private static final Integer DEFAULT_CLICK_THRESHOLD_TIME = 300;
  private static final Integer ANIMATION_TIME = 500;
  private static final Integer ANIMATION_STEP = 5;
  private static final Double REMOVE_EXPAND_SCALE = 1.5;
  private static final Integer DEFAULT_CLICK_THRESHOLD_PX = 10;

  // Constructor Variable
  private FloatingBubbleLogger logger;

  // The Window Manager View
  private WindowManager windowManager;

  // The Bubble View
  private ImageView bubbleView;

  // Remove View
  private ImageView removeBubbleView;

  // Initial X,Y coordinates
  private int initialXCoordinate;
  private int initialYCoordinate;
  private int floatingBubbleInitialX;
  private int floatingBubbleInitialY;

  private Point windowSize = new Point();

  @Override
  public void onCreate() {
    super.onCreate();
    logger = new FloatingBubbleLogger().setDebugEnabled(true).setTag(TAG);
  }

  /**
   * Get the default window layout params
   *
   * @return the layout param
   */
  private WindowManager.LayoutParams getDefaultWindowParams() {
    return new WindowManager.LayoutParams(
        WindowManager.LayoutParams.WRAP_CONTENT,
        WindowManager.LayoutParams.WRAP_CONTENT,
        WindowManager.LayoutParams.TYPE_PHONE,
        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
        | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
        | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
        PixelFormat.TRANSLUCENT);
  }

  /**
   * Creates the views
   */
  private void createViews() {
    // Load the Window Managers
    windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
    LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);

    // Setting up the Remove Bubble View
    removeBubbleView = (ImageView) inflater.inflate(R.layout.remove_bubble_view, null);
    WindowManager.LayoutParams bubbleRemoveLayoutParams = getDefaultWindowParams();
    bubbleRemoveLayoutParams.gravity = Gravity.TOP | Gravity.START;
    removeBubbleView.setVisibility(View.GONE);
    windowManager.addView(removeBubbleView, bubbleRemoveLayoutParams);

    // Setting up the Floating Bubble View
    bubbleView = (ImageView) inflater.inflate(R.layout.floating_bubble_view, null);
    windowManager.getDefaultDisplay().getSize(windowSize);

    WindowManager.LayoutParams bubbleParams = getDefaultWindowParams();
    bubbleParams.gravity = Gravity.TOP | Gravity.START;
    bubbleParams.x = 0;
    bubbleParams.y = windowSize.y / 2 - bubbleView.getHeight() / 2;
    windowManager.addView(bubbleView, bubbleParams);

    /**
     * Setting up the Chat Head touch listener
     */
    bubbleView.setOnTouchListener(new View.OnTouchListener() {
      long startTime = 0;
      long endTime = 0;
      boolean isRemoveActive = false;
      boolean isInsideRemoveBound = false;
      int removeInitialBubbleWidth = 0;
      int removeInitialBubbleHeight = 0;

      Handler longTouchHandler = new Handler();
      Runnable longTouchRunnable = new Runnable() {

        @Override
        public void run() {
          isRemoveActive = true;
          showRemoveView();
        }
      };

      @Override
      public boolean onTouch(View v, MotionEvent event) {
        WindowManager.LayoutParams bubbleParams =
            (WindowManager.LayoutParams) bubbleView.getLayoutParams();

        int currentXCoordinate = (int) event.getRawX();
        int currentYCoordinate = (int) event.getRawY();
        int destinationXCoordinate;
        int destinationYCoordinate;

        switch (event.getAction()) {
          case MotionEvent.ACTION_DOWN:
            startTime = System.currentTimeMillis();
            longTouchHandler.postDelayed(longTouchRunnable, DEFAULT_CLICK_THRESHOLD_TIME);

            initialXCoordinate = currentXCoordinate;
            initialYCoordinate = currentYCoordinate;

            removeInitialBubbleWidth = removeBubbleView.getLayoutParams().width;
            removeInitialBubbleHeight = removeBubbleView.getLayoutParams().height;

            floatingBubbleInitialX = bubbleParams.x;
            floatingBubbleInitialY = bubbleParams.y;
            break;

          case MotionEvent.ACTION_MOVE:
            int motionX = currentXCoordinate - initialXCoordinate;
            int motionY = currentYCoordinate - initialYCoordinate;

            destinationXCoordinate = floatingBubbleInitialX + motionX;
            destinationYCoordinate = floatingBubbleInitialY + motionY;

            if (isRemoveActive) {
              int boundLeft = windowSize.x / 2 - (int) (removeInitialBubbleWidth
                                                        * REMOVE_EXPAND_SCALE);
              int boundRight = windowSize.x / 2 + (int) (removeInitialBubbleWidth
                                                         * REMOVE_EXPAND_SCALE);
              int boundTop = windowSize.y - (int) (removeInitialBubbleHeight * REMOVE_EXPAND_SCALE);

              if ((currentXCoordinate >= boundLeft
                   && currentXCoordinate <= boundRight)
                  && currentYCoordinate >= boundTop) {
                isInsideRemoveBound = true;

                int removeViewXCoordinate =
                    (int) ((windowSize.x - (removeInitialBubbleHeight * REMOVE_EXPAND_SCALE)) / 2);
                int removeViewYCoordinate =
                    (int) (windowSize.y - ((removeInitialBubbleWidth * REMOVE_EXPAND_SCALE)
                                           + getBottomMargin()));

                if (removeBubbleView.getLayoutParams().height == removeInitialBubbleHeight) {
                  removeBubbleView.getLayoutParams().height =
                      (int) (removeInitialBubbleHeight * REMOVE_EXPAND_SCALE);
                  removeBubbleView.getLayoutParams().width =
                      (int) (removeInitialBubbleWidth * REMOVE_EXPAND_SCALE);

                  WindowManager.LayoutParams paramRemove =
                      (WindowManager.LayoutParams) removeBubbleView.getLayoutParams();
                  paramRemove.x = removeViewXCoordinate;
                  paramRemove.y = removeViewYCoordinate;

                  windowManager.updateViewLayout(removeBubbleView, paramRemove);
                }

                // Set the chat head to the center of remove_bubble_view view
                bubbleParams.x =
                    removeViewXCoordinate
                    + (Math.abs(removeBubbleView.getWidth() - bubbleView.getWidth())) / 2;
                bubbleParams.y =
                    removeViewYCoordinate
                    + (Math.abs(removeBubbleView.getHeight() - bubbleView.getHeight())) / 2;
                windowManager.updateViewLayout(bubbleView, bubbleParams);
                break;
              } else {
                // The remove_bubble_view view is not under the bubble view
                isInsideRemoveBound = false;
                removeBubbleView.getLayoutParams().height = removeInitialBubbleHeight;
                removeBubbleView.getLayoutParams().width = removeInitialBubbleWidth;

                int removeViewXCoordinate = (windowSize.x - removeBubbleView.getWidth()) / 2;
                int removeViewYCoordinate = windowSize.y - (removeBubbleView.getHeight() +
                                                            getBottomMargin());

                WindowManager.LayoutParams removeViewParams =
                    (WindowManager.LayoutParams) removeBubbleView.getLayoutParams();
                removeViewParams.x = removeViewXCoordinate;
                removeViewParams.y = removeViewYCoordinate;

                windowManager.updateViewLayout(removeBubbleView, removeViewParams);
              }
            }

            // Update the position of the chat head
            bubbleParams.x = destinationXCoordinate;
            bubbleParams.y = destinationYCoordinate;
            windowManager.updateViewLayout(bubbleView, bubbleParams);

            break;
          case MotionEvent.ACTION_UP:
            isRemoveActive = false;
            removeBubbleView.setVisibility(View.GONE);
            removeBubbleView.getLayoutParams().height = removeInitialBubbleHeight;
            removeBubbleView.getLayoutParams().width = removeInitialBubbleWidth;
            longTouchHandler.removeCallbacks(longTouchRunnable);

            if (isInsideRemoveBound) {
              stopSelf();
              isInsideRemoveBound = false;
              break;
            }


            int xDiff = currentXCoordinate - initialXCoordinate;
            int yDiff = currentYCoordinate - initialYCoordinate;

            if (Math.abs(xDiff) < DEFAULT_CLICK_THRESHOLD_PX
                && Math.abs(yDiff) < DEFAULT_CLICK_THRESHOLD_PX) {
              endTime = System.currentTimeMillis();
              if ((endTime - startTime) < DEFAULT_CLICK_THRESHOLD_TIME) {
                chatHeadClick();
              }
            }

            destinationYCoordinate = floatingBubbleInitialY + yDiff;

            // Reset Y Coordinate
            int bottomMargin = getBottomMargin();
            if (destinationYCoordinate < 0) {
              destinationYCoordinate = 0;
            } else if (destinationYCoordinate + (bubbleView.getHeight() + bottomMargin) >
                       windowSize.y) {
              destinationYCoordinate = windowSize.y - (bubbleView.getHeight() + bottomMargin);
            }
            bubbleParams.y = destinationYCoordinate;
            // Reset X Coordinate
            resetPosition(currentXCoordinate);

            isInsideRemoveBound = false;

            break;
          default:
            logger.log("bubbleView.setOnTouchListener  -> event.getAction() : default");
            break;
        }
        return true;
      }
    });
  }


  @Override
  public void onConfigurationChanged(Configuration newConfig) {
    super.onConfigurationChanged(newConfig);

    windowManager.getDefaultDisplay().getSize(windowSize);
    WindowManager.LayoutParams layoutParams =
        (WindowManager.LayoutParams) bubbleView.getLayoutParams();

    if (layoutParams.y + (bubbleView.getHeight() + getBottomMargin()) > windowSize.y) {
      layoutParams.y = windowSize.y - (bubbleView.getHeight() + getBottomMargin());
      windowManager.updateViewLayout(bubbleView, layoutParams);
    }
    resetPosition(windowSize.x);
  }

  /**
   * Resets the position of the chat head to the corner
   *
   * @param currentXCoordinate the current x coordinate of the head
   */
  private void resetPosition(int currentXCoordinate) {
    if (currentXCoordinate <= windowSize.x / 2) {
      moveToLeft(currentXCoordinate);
    } else {
      moveToRight(currentXCoordinate);
    }
  }

  /**
   * Move chat head to the top corner
   *
   * @param currentYCoordinate the coordinate value
   */
  private void moveToTop(final int currentYCoordinate, final int finalCoordinate) {
    final int y = windowSize.y - currentYCoordinate;

    new CountDownTimer(ANIMATION_TIME, ANIMATION_STEP) {
      WindowManager.LayoutParams bubbleParams =
          (WindowManager.LayoutParams) bubbleView.getLayoutParams();

      public void onTick(long t) {
        long step = (ANIMATION_TIME - t) / ANIMATION_STEP;
        bubbleParams.y = finalCoordinate - (int) (double) motionValue(step, y);
        windowManager.updateViewLayout(bubbleView, bubbleParams);
      }

      public void onFinish() {
        bubbleParams.y = finalCoordinate;
        windowManager.updateViewLayout(bubbleView, bubbleParams);
      }
    }.start();
  }

  /**
   * Move chat head to the left corner
   *
   * @param currentXCoordinate the coordinate value
   */
  private void moveToLeft(final int currentXCoordinate) {
    final int x = windowSize.x - currentXCoordinate;

    new CountDownTimer(ANIMATION_TIME, ANIMATION_STEP) {
      WindowManager.LayoutParams bubbleParams =
          (WindowManager.LayoutParams) bubbleView.getLayoutParams();

      public void onTick(long t) {
        long step = (ANIMATION_TIME - t) / ANIMATION_STEP;
        bubbleParams.x = 0 - (int) (double) motionValue(step, x);
        windowManager.updateViewLayout(bubbleView, bubbleParams);
      }

      public void onFinish() {
        bubbleParams.x = 0;
        windowManager.updateViewLayout(bubbleView, bubbleParams);
      }
    }.start();
  }

  /**
   * Move chat head to the right corner
   *
   * @param currentXCoordinate the coordinate value
   */
  private void moveToRight(final int currentXCoordinate) {
    new CountDownTimer(ANIMATION_TIME, ANIMATION_STEP) {
      WindowManager.LayoutParams bubbleParams =
          (WindowManager.LayoutParams) bubbleView.getLayoutParams();

      public void onTick(long t) {
        long step = (ANIMATION_TIME - t) / ANIMATION_STEP;
        bubbleParams.x = windowSize.x
                         + (int) (double) motionValue(step, currentXCoordinate)
                         - bubbleView.getWidth();
        windowManager.updateViewLayout(bubbleView, bubbleParams);
      }

      public void onFinish() {
        bubbleParams.x = windowSize.x - bubbleView.getWidth();
        windowManager.updateViewLayout(bubbleView, bubbleParams);
      }
    }.start();
  }

  /**
   * The motion amount
   *
   * @param step  the step count from 1 -> 0
   * @param scale the distance to step
   * @return the value
   */
  private double motionValue(long step, long scale) {
    return scale * java.lang.Math.exp(-0.1 * step);
  }

  /**
   * Converts DPs to Pixel values
   *
   * @return the pixel value
   */
  private int dpToPixels(int marginDp) {
    DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
    return Math.round(marginDp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
  }

  /**
   * Returns the bottom margin in pixels
   *
   * @return bottom margin
   */
  private int getBottomMargin() {
    return dpToPixels(REMOVE_BOTTOM_MARGIN);
  }

  /**
   * Handles the chat head click - opens the activity
   */
  private void chatHeadClick() {

  }

  /**
   * Displays the remove view on the bottom
   */
  private void showRemoveView() {
    removeBubbleView.setVisibility(View.VISIBLE);

    int removeViewX = (windowSize.x - removeBubbleView.getWidth()) / 2;
    int removeViewY = windowSize.y - (removeBubbleView.getHeight() + getBottomMargin());

    WindowManager.LayoutParams removeViewParams =
        (WindowManager.LayoutParams) removeBubbleView.getLayoutParams();
    removeViewParams.x = removeViewX;
    removeViewParams.y = removeViewY;

    windowManager.updateViewLayout(removeBubbleView, removeViewParams);
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    logger.setDebugEnabled(intent.getBooleanExtra(
        FloatingBubbleServiceBuilder.IntentKeys.DEBUG_MODE.name(),
        false));

    loadIntentExtras(intent);
    if (startId == Service.START_STICKY) {
      logger.log("Start with START_STICKY");
      createViews();
      return super.onStartCommand(intent, flags, startId);
    } else {
      logger.log("Start with START_NOT_STICKY, not doing anything");
      return Service.START_NOT_STICKY;
    }
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    if (bubbleView != null) {
      windowManager.removeView(bubbleView);
    }

    if (removeBubbleView != null) {
      windowManager.removeView(removeBubbleView);
    }
  }


  @Override
  public IBinder onBind(Intent intent) {
    return null;
  }

  private void loadIntentExtras(Intent intent) {
    logger.log("Loading intent extras");
    Integer removeImageResource = intent.getIntExtra(
        REMOVE_IMAGE_RESOURCE.name(),
        DEFAULT_EMPTY_VALUE);
    if (!removeImageResource.equals(DEFAULT_EMPTY_VALUE)) {
      logger.log("Setting Remove Bubble Icon");
      removeBubbleView.setImageResource(removeImageResource);
    }

    Integer bubbleImageResource = intent.getIntExtra(
        BUBBLE_IMAGE_RESOURCE.name(),
        DEFAULT_EMPTY_VALUE);
    if (!bubbleImageResource.equals(DEFAULT_EMPTY_VALUE)) {
      logger.log("Setting Bubble Icon");
      bubbleView.setImageResource(bubbleImageResource);
    }
  }
}
