# Floating Bubble Library
Simple library for adding a floating bubble in your application!

## Installation
```java

```

## Usage

### Setup
Let's start with a simple setup for the Service
```java
public class FloatingService extends FloatingBubbleService {
    ...
}
```

Adding your library in the manifest
```java
<service android:name="<YOUR_PACKAGE>.FloatingService" />
```

Start the service
```java
startService(new Intent(view.getContext(), FloatingService.class));
```

### Customising the Service
```java
public class FloatingService extends FloatingBubbleService {

  @Override
  protected FloatingBubbleConfig getConfig() {
    return new FloatingBubbleConfig.Builder()
        .bubbleIcon(bubbleDrawable) // Set the drawable for the bubble
        .removeBubbleIcon(removeIconDrawable) // Set the drawable for the remove bubble
        .bubbleIconDp(64) // Set the size of the bubble in dp
        .removeBubbleIconDp(64) // Set the size of the remove bubble in dp
        .paddingDp(4) // Set the padding of the view from the boundary
        .physicsEnabled(true) // Does the bubble attract towards the walls
        .expandableColor(Color.WHITE) // The color of the triangable and background of the layout
        .gravity(Gravity.END) // Horizontal gravity of the bubble when expanded
        .expandableView(bubbleView) // The view which is visible in the expanded view
        .build();
  }
}
```

## License
```java
Copyright 2016 Bijoy Singh Kochar

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```