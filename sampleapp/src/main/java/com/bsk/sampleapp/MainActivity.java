package com.bsk.sampleapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.bsk.floatingbubblelib.permission.PermissionUtils;
import com.bsk.floatingbubblelib.service.FloatingBubbleServiceBuilder;

public class MainActivity extends AppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    PermissionUtils.startPermissionRequest(this);
    View startBubble = findViewById(R.id.start_bubble);
    startBubble.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        Intent serviceIntent =
            new FloatingBubbleServiceBuilder(view.getContext())
                .setDebugMode(true)
                .build();
        startService(serviceIntent);
      }
    });
  }
}
