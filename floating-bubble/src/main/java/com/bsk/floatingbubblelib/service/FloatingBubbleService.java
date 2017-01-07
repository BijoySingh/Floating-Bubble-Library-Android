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
  private static final Integer ANIMATION_GAP = 20;
  private static final Integer ANIMATION_STEPS = 5;
  private static final Double REMOVE_EXPAND_SCALE = 1.5;
  private static final Double MAX_VELOCITY = 100.0;
  private static final Double MIN_VELOCITY = 30.0;
  private static final Integer DEFAULT_CLICK_THRESHOLD_PX = 10;
  private static final Integer BUBBLE_SIZE = 64;

  // Constructor Variable
  private FloatingBubbleLogger logger;

  // The Window Manager View
  private WindowManager windowManager;

  // The Bubble View
  private ImageView bubbleView;

  // Remove View
  private ImageView removeBubbleView;

  // Window Dimensions
  private Point windowSize = new Point();

  // Bubble Listeners
  private BubbleTouchListener bubbleTouchListener;
  private BubbleListener bubbleListener;

  /**
   * ========= BASIC SETUP FUNCTIONS =========
   */

  @Override
  public void onCreate() {
    super.onCreate();
    logger = new FloatingBubbleLogger().setDebugEnabled(true).setTag(TAG);
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

    logger.setDebugEnabled(intent.getBooleanExtra(
        FloatingBubbleServiceBuilder.IntentKeys.DEBUG_MODE.name(),
        false));

    loadIntentExtras(intent);
    if (startId == Service.START_STICKY) {
      logger.log("Start with START_STICKY");
      createViews();
      if (bubbleListener != null) {
        bubbleListener.onBubbleAdded();
      }
      return super.onStartCommand(intent, flags, startId);
    } else {
      logger.log("Start with START_NOT_STICKY, not doing anything");
      return Service.START_NOT_STICKY;
    }
  }


  @Override
  public void onDestroy() {
    super.onDestroy();
    logger.log("onDestroy");

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

    windowManager.getDefaultDisplay().getSize(windowSize);
    WindowManager.LayoutParams layoutParams =
        (WindowManager.LayoutParams) bubbleView.getLayoutParams();

    if (layoutParams.y + (bubbleView.getHeight() + getBottomMargin()) > windowSize.y) {
      layoutParams.y = windowSize.y - (bubbleView.getHeight() + getBottomMargin());
      windowManager.updateViewLayout(bubbleView, layoutParams);
    }
    moveXToCorner(windowSize.x);
  }

  /**
   * Load the variables from the Intent
   *
   * @param intent intent for the service
   */
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

    // Setting up view
    removeBubbleView = (ImageView) inflater.inflate(R.layout.remove_bubble_view, null);
    bubbleView = (ImageView) inflater.inflate(R.layout.floating_bubble_view, null);

    // Remove Bubble View setup
    WindowManager.LayoutParams bubbleRemoveLayoutParams = getDefaultWindowParams();
    bubbleRemoveLayoutParams.gravity = Gravity.TOP | Gravity.START;
    bubbleRemoveLayoutParams.width = dpToPixels(BUBBLE_SIZE);
    bubbleRemoveLayoutParams.height = dpToPixels(BUBBLE_SIZE);
    removeBubbleView.setVisibility(View.GONE);
    windowManager.addView(removeBubbleView, bubbleRemoveLayoutParams);

    // Setting up the Floating Bubble View
    windowManager.getDefaultDisplay().getSize(windowSize);

    WindowManager.LayoutParams bubbleParams = getDefaultWindowParams();
    bubbleParams.width = dpToPixels(BUBBLE_SIZE);
    bubbleParams.height = dpToPixels(BUBBLE_SIZE);
    bubbleParams.gravity = Gravity.TOP | Gravity.START;
    bubbleParams.x = 0;
    bubbleParams.y = windowSize.y / 2 - bubbleView.getHeight() / 2;
    windowManager.addView(bubbleView, bubbleParams);

    /**
     * Setting up the Chat Head touch listener
     */
    bubbleView.setOnTouchListener(new BubbleOnTouchListener());
  }

  /**
   * ========= POSITION FUNCTIONS =========
   */

  /**
   * Resets the position of the bubble to the corner
   *
   * @param currentYCoordinate the current y coordinate of the head
   */
  private void moveYToCorner(int currentYCoordinate) {
    if (currentYCoordinate <= windowSize.y / 2) {
      moveToTop(currentYCoordinate);
    } else {
      moveToBottom(currentYCoordinate);
    }
  }


  /**
   * Resets the position of the bubble to the corner
   *
   * @param currentXCoordinate the current x coordinate of the head
   */
  private void moveXToCorner(int currentXCoordinate) {
    if (currentXCoordinate <= windowSize.x / 2) {
      moveToLeft(currentXCoordinate);
    } else {
      moveToRight(currentXCoordinate);
    }
  }

  /**
   * Resets the position of the bubble to top/bottom if it's out of bound
   */
  private void resetYPosition(int bubbleY) {
    // Reset Y Coordinate
    int bottomMargin = getBottomMargin();
    if (bubbleY < 0) {
      bubbleY = 0;
    } else if (bubbleY + (bubbleView.getHeight() + bottomMargin) >
               windowSize.y) {
      bubbleY = windowSize.y - (bubbleView.getHeight() + bottomMargin);
    }
    setBubbleViewPositionY(bubbleY);
  }


  /**
   * Move chat head to the top corner
   *
   * @param currentYCoordinate the coordinate value
   */
  private void moveToTop(final int currentYCoordinate) {
    final int y = currentYCoordinate;
    new CountDownTimer(ANIMATION_TIME, ANIMATION_STEPS) {
      public void onTick(long t) {
        long step = (ANIMATION_TIME - t) / ANIMATION_STEPS;
        setBubbleViewPositionX((int) (double) motionValue(step, y));
      }

      public void onFinish() {
        setBubbleViewPositionY(0);
      }
    }.start();
  }

  /**
   * Move chat head to the bottom corner
   *
   * @param currentYCoordinate the coordinate value
   */
  private void moveToBottom(final int currentYCoordinate) {
    new CountDownTimer(ANIMATION_TIME, ANIMATION_STEPS) {
      public void onTick(long t) {
        long step = (ANIMATION_TIME - t) / ANIMATION_STEPS;
        setBubbleViewPositionY(
            windowSize.y
            + (int) (double) motionValue(step, currentYCoordinate)
            - bubbleView.getHeight());
      }

      public void onFinish() {
        setBubbleViewPositionY(
            windowSize.y - bubbleView.getHeight() - getBottomMargin());
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
    new CountDownTimer(ANIMATION_TIME, ANIMATION_STEPS) {
      public void onTick(long t) {
        long step = (ANIMATION_TIME - t) / ANIMATION_STEPS;
        setBubbleViewPositionX(0 - (int) (double) motionValue(step, x));
      }

      public void onFinish() {
        setBubbleViewPositionX(0);
      }
    }.start();
  }

  /**
   * Move chat head to the right corner
   *
   * @param currentXCoordinate the coordinate value
   */
  private void moveToRight(final int currentXCoordinate) {
    new CountDownTimer(ANIMATION_TIME, ANIMATION_STEPS) {
      public void onTick(long t) {
        long step = (ANIMATION_TIME - t) / ANIMATION_STEPS;
        setBubbleViewPositionX(
            windowSize.x
            + (int) (double) motionValue(step, currentXCoordinate)
            - bubbleView.getWidth());
      }

      public void onFinish() {
        setBubbleViewPositionX(windowSize.x - bubbleView.getWidth());
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
    return scale * Math.exp(-0.1 * step);
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

  /**
   * ========= INTERFACES =========
   */

  /**
   * Bubble Touch Listener interface
   */
  public interface BubbleTouchListener {
    void onTap();

    void onTouchDown();

    void onTouchHold();

    void onTouchUp();
  }

  /**
   * Bubble basic event listeners
   */
  public interface BubbleListener {
    void onBubbleAdded();

    void onBubbleRemoved();
  }

  protected void setBubbleTouchListener(BubbleTouchListener listener) {
    this.bubbleTouchListener = listener;
  }

  protected void setBubbleListener(BubbleListener listener) {
    this.bubbleListener = listener;
  }

  /**
   * ========= BUBBLE TOUCH LISTENER =========
   */

  public class BubbleOnTouchListener implements View.OnTouchListener {
    long touchDownTime = 0;
    long endTime = 0;
    boolean isRemoveActive = false;
    boolean isInsideRemoveBound = false;
    int removeInitialBubbleWidth = 0;
    int removeInitialBubbleHeight = 0;

    // Initial X,Y coordinates
    private long previousTime;
    private int initialTouchX;
    private int initialTouchY;
    private int previousTouchX;
    private int previousTouchY;
    private int floatingBubbleInitialX;
    private int floatingBubbleInitialY;

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

      int currentTouchX = (int) event.getRawX();
      int currentTouchY = (int) event.getRawY();
      int destinationTouchX;
      int destinationTouchY;

      switch (event.getAction()) {
        case MotionEvent.ACTION_DOWN:
          touchDownTime = System.currentTimeMillis();
          showDelayedRemoveBubbleView();

          initialTouchX = currentTouchX;
          initialTouchY = currentTouchY;

          removeInitialBubbleWidth = removeBubbleView.getLayoutParams().width;
          removeInitialBubbleHeight = removeBubbleView.getLayoutParams().height;

          floatingBubbleInitialX = bubbleParams.x;
          floatingBubbleInitialY = bubbleParams.y;

          if (bubbleTouchListener != null) {
            bubbleTouchListener.onTouchDown();
          }
          break;

        case MotionEvent.ACTION_MOVE:
          int motionX = currentTouchX - initialTouchX;
          int motionY = currentTouchY - initialTouchY;

          destinationTouchX = floatingBubbleInitialX + motionX;
          destinationTouchY = floatingBubbleInitialY + motionY;

          if (isRemoveActive) {
            if (bubbleTouchListener != null) {
              bubbleTouchListener.onTouchHold();
            }

            int boundLeft = windowSize.x / 2
                            - (int) (removeInitialBubbleWidth * REMOVE_EXPAND_SCALE);
            int boundRight = windowSize.x / 2
                             + (int) (removeInitialBubbleWidth * REMOVE_EXPAND_SCALE);
            int boundTop = windowSize.y - (int) (removeInitialBubbleHeight * REMOVE_EXPAND_SCALE);

            if (currentTouchX >= boundLeft
                && currentTouchX <= boundRight
                && currentTouchY >= boundTop) {
              isInsideRemoveBound = true;

              int removeViewXCoordinate =
                  (int) ((windowSize.x - (removeInitialBubbleHeight * REMOVE_EXPAND_SCALE)) / 2);
              int removeViewYCoordinate =
                  (int) (windowSize.y - ((removeInitialBubbleWidth * REMOVE_EXPAND_SCALE)
                                         + getBottomMargin()));

              if (removeBubbleView.getLayoutParams().height == removeInitialBubbleHeight) {
                setRemoveViewSize(
                    (int) (removeInitialBubbleWidth * REMOVE_EXPAND_SCALE),
                    (int) (removeInitialBubbleHeight * REMOVE_EXPAND_SCALE));
                setRemoveViewPositionX(removeViewXCoordinate);
                setRemoveViewPositionY(removeViewYCoordinate);
              }

              // Set the chat head to the center of remove_bubble_view view
              setBubbleViewPositionX(
                  removeViewXCoordinate
                  + (Math.abs(removeBubbleView.getWidth() - bubbleView.getWidth())) / 2);
              setBubbleViewPositionY(
                  removeViewYCoordinate
                  + (Math.abs(removeBubbleView.getHeight() - bubbleView.getHeight())) / 2);
              break;
            } else {
              // The remove_bubble_view view is not under the bubble view
              isInsideRemoveBound = false;
              setRemoveViewSize(removeInitialBubbleWidth, removeInitialBubbleHeight);
              int removeViewXCoordinate = (windowSize.x - removeBubbleView.getWidth()) / 2;
              int removeViewYCoordinate = windowSize.y - (removeBubbleView.getHeight() +
                                                          getBottomMargin());
              setRemoveViewPositionX(removeViewXCoordinate);
              setRemoveViewPositionY(removeViewYCoordinate);
            }
          }

          // Update the position of the chat head
          setBubbleViewPositionX(destinationTouchX);
          setBubbleViewPositionY(destinationTouchY);
          break;
        case MotionEvent.ACTION_UP:
          if (bubbleTouchListener != null) {
            bubbleTouchListener.onTouchUp();
          }

          hideRemoveBubbleView();
          if (isInsideRemoveBound) {
            removeFloatingBubble();
            break;
          }

          int xMotion = currentTouchX - initialTouchX;
          int yMotion = currentTouchY - initialTouchY;
          checkIfTap(xMotion, yMotion);

          animateBubbleMotion(
              xMotion,
              yMotion);
          // resetYPosition(floatingBubbleInitialY + yMotion);
          // moveXToCorner(currentTouchX);
          // moveYToCorner(currentTouchY);
          isInsideRemoveBound = false;
          break;

        default:
          logger.log("bubbleView.setOnTouchListener  -> event.getAction() : default");
          break;
      }

      previousTouchX = currentTouchX;
      previousTouchY = currentTouchY;
      previousTime = System.currentTimeMillis();
      return true;
    }

    private void showDelayedRemoveBubbleView() {
      longTouchHandler.postDelayed(longTouchRunnable, DEFAULT_CLICK_THRESHOLD_TIME);
    }

    private void hideRemoveBubbleView() {
      isRemoveActive = false;
      removeBubbleView.setVisibility(View.GONE);
      setRemoveViewSize(removeInitialBubbleWidth, removeInitialBubbleHeight);
      longTouchHandler.removeCallbacks(longTouchRunnable);
    }

    private void removeFloatingBubble() {
      if (bubbleListener != null) {
        bubbleListener.onBubbleRemoved();
      }
      stopSelf();
      isInsideRemoveBound = false;
    }

    private void checkIfTap(int xMotion, int yMotion) {
      if (Math.abs(xMotion) < DEFAULT_CLICK_THRESHOLD_PX
          && Math.abs(yMotion) < DEFAULT_CLICK_THRESHOLD_PX) {
        endTime = System.currentTimeMillis();
        if ((endTime - touchDownTime) < DEFAULT_CLICK_THRESHOLD_TIME
            && bubbleTouchListener != null) {
          bubbleTouchListener.onTap();
        }
      }
    }
  }

  /**
   * ========= VIEW LAYOUT PARAMS EDIT METHODS =========
   */

  /**
   * Animates the bubble motion
   *
   * @param vx the x velocity
   * @param vy the y velocity
   */
  public void animateBubbleMotion(double vx, double vy) {
    double v = Math.hypot(vx, vy);
    if (v > MAX_VELOCITY) {
      vy = MAX_VELOCITY * (vy / v);
      vx = MAX_VELOCITY * (vx / v);
    } else if (v < MIN_VELOCITY && v > 0.1) {
      WindowManager.LayoutParams bubbleParams =
          (WindowManager.LayoutParams) bubbleView.getLayoutParams();
      int x = bubbleParams.x;
      int y = bubbleParams.y;
      moveXToCorner(x);
      if (!(y > 0 && y < windowSize.y)) {
        moveYToCorner(y);
      }
    }

    final double vxf = vx;
    final double vyf = vy;

    final Handler handler = new Handler();
    handler.postDelayed(new Runnable() {

      double cvx = vxf;
      double cvy = vyf;

      @Override
      public void run() {
        WindowManager.LayoutParams bubbleParams =
            (WindowManager.LayoutParams) bubbleView.getLayoutParams();
        int x = bubbleParams.x;
        int y = bubbleParams.y;
        if (x > 0 && x < windowSize.x - bubbleParams.width) {
          setBubbleViewPositionX((int) (x + cvx));
        } else {
          moveXToCorner(x);
          if (!(y > 0 && y < windowSize.y)) {
            moveYToCorner(y);
          }
          return;
        }

        if (y > 0 && y < windowSize.y) {
          setBubbleViewPositionY((int) (y + cvy));
        } else {
          moveYToCorner(y);
          return;
        }

        cvy /= 1.05;
        if (Math.abs(cvy) < 1 && Math.abs(cvx) < 10) {
          moveXToCorner(x);
        }
        handler.postDelayed(this, ANIMATION_GAP);
      }
    }, ANIMATION_GAP);
  }

  /**
   * Bubble Size setting
   *
   * @param width  width
   * @param height height
   */
  private void setBubbleViewSize(int width, int height) {
    bubbleView.getLayoutParams().width = width;
    bubbleView.getLayoutParams().height = height;
  }

  /**
   * Remove Bubble Size setting
   *
   * @param width  width
   * @param height height
   */
  private void setRemoveViewSize(int width, int height) {
    removeBubbleView.getLayoutParams().width = width;
    removeBubbleView.getLayoutParams().height = height;
  }

  /**
   * Remove Bubble Position X
   *
   * @param x X px coordinate
   */
  private void setRemoveViewPositionX(int x) {
    WindowManager.LayoutParams bubbleParams =
        (WindowManager.LayoutParams) removeBubbleView.getLayoutParams();
    bubbleParams.x = x;
    windowManager.updateViewLayout(removeBubbleView, bubbleParams);
  }

  /**
   * Remove Bubble Position Y
   *
   * @param y Y px coordinate
   */
  private void setRemoveViewPositionY(int y) {
    WindowManager.LayoutParams bubbleParams =
        (WindowManager.LayoutParams) removeBubbleView.getLayoutParams();
    bubbleParams.y = y;
    windowManager.updateViewLayout(removeBubbleView, bubbleParams);
  }

  /**
   * Bubble View Position X
   *
   * @param x x px coordinate
   */
  private void setBubbleViewPositionX(int x) {
    WindowManager.LayoutParams bubbleParams =
        (WindowManager.LayoutParams) bubbleView.getLayoutParams();
    bubbleParams.x = x;
    windowManager.updateViewLayout(bubbleView, bubbleParams);
  }

  /**
   * Bubble View Position Y
   *
   * @param y y px coordinate
   */
  private void setBubbleViewPositionY(int y) {
    WindowManager.LayoutParams bubbleParams =
        (WindowManager.LayoutParams) bubbleView.getLayoutParams();
    bubbleParams.y = y;
    windowManager.updateViewLayout(bubbleView, bubbleParams);
  }
}
