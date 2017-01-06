package com.bsk.floatingbubblelib.permission;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

/**
 * Utilities for handling permissions
 * Created by bijoy on 1/6/17.
 */

public class PermissionUtils {
  public static final Integer REQUEST_CODE_ASK_PERMISSIONS = 1201;

  public static boolean requiresPermission(Activity activity) {
    return Build.VERSION.SDK_INT >= 23 && !Settings.canDrawOverlays(activity);
  }

  public static void startPermissionRequest(Activity activity) {
    if (Build.VERSION.SDK_INT >= 23 && requiresPermission(activity)) {
      Intent intent = new Intent(
          Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
          Uri.parse("package:" + activity.getPackageName()));
      activity.startActivityForResult(
          intent,
          REQUEST_CODE_ASK_PERMISSIONS);
    }
  }
}
