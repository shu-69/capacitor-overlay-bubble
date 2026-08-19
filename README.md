# capacitor-overlay-bubble

A native Capacitor plugin for Android that provides a floating, draggable overlay bubble UI (similar to Messenger/WhatsApp call bubbles) for active VoIP/video/voice call continuity.

[![npm version](https://badge.fury.io/js/capacitor-overlay-bubble.svg)](https://badge.fury.io/js/capacitor-overlay-bubble)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

## Features

- 🎈 **Draggable Floating Overlay Bubble**: Renders a floating avatar/initials bubble over other apps using Android `WindowManager`.
- ⏱️ **Live Duration Badge**: Dynamically updates call timer badge attached to the bubble.
- 🎯 **Drag to Dismiss**: Dragging the bubble down to the bottom screen target dismisses the overlay.
- 📱 **Tap to Return**: Tapping the bubble brings your application back to the foreground.
- 🔐 **Permission Management**: Built-in methods to check and request `SYSTEM_ALERT_WINDOW` permission on Android.

---

## Installation

```bash
npm install capacitor-overlay-bubble
npx cap sync
```

### Git Dependency Installation

```bash
npm install git+https://github.com/<your-org-or-user>/capacitor-overlay-bubble.git#v1.0.0
npx cap sync
```

---

## Android Configuration

Add the `SYSTEM_ALERT_WINDOW` permission to your app's `android/app/src/main/AndroidManifest.xml`:

```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    <uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW" />
</manifest>
```

> **Note**: Android 10+ (API 29+) requires user consent via System Settings ("Display over other apps"). Use `requestPermission()` to direct the user to grant this setting.

---

## API Reference

```typescript
import { OverlayBubble } from 'capacitor-overlay-bubble';
```

### Methods

| Method | Return Type | Description |
| :--- | :--- | :--- |
| `checkPermission()` | `Promise<OverlayBubblePermissionStatus>` | Checks if `SYSTEM_ALERT_WINDOW` permission is granted. |
| `requestPermission()` | `Promise<OverlayBubblePermissionStatus>` | Opens Android System Settings for overlay permission. |
| `showBubble(options)` | `Promise<void>` | Displays the floating draggable overlay bubble. |
| `updateBubble(options)` | `Promise<void>` | Updates duration timer, avatar, or callee name on active bubble. |
| `hideBubble()` | `Promise<void>` | Hides and removes floating overlay bubble from screen. |

### Interfaces

#### `ShowBubbleOptions`
```typescript
interface ShowBubbleOptions {
  avatarUrl?: string;
  calleeName?: string;
  callType?: 'video' | 'voice';
  durationSeconds?: number;
}
```

#### `UpdateBubbleOptions`
```typescript
interface UpdateBubbleOptions {
  durationSeconds?: number;
  avatarUrl?: string;
  calleeName?: string;
}
```

#### `OverlayBubblePermissionStatus`
```typescript
interface OverlayBubblePermissionStatus {
  granted: boolean;
}
```

---

## Event Listeners

### `bubbleTapped`
Fired when the user single-taps the floating overlay bubble.

```typescript
const listener = await OverlayBubble.addListener('bubbleTapped', () => {
  console.log('User tapped bubble to return to call screen');
});

// To remove listener:
listener.remove();
```

### `bubbleDismissed`
Fired when the user drags the floating bubble into the bottom dismiss zone.

```typescript
const listener = await OverlayBubble.addListener('bubbleDismissed', () => {
  console.log('User dragged bubble to dismiss zone');
});
```

---

## Example Usage (Angular / TypeScript)

```typescript
import { Injectable } from '@angular/core';
import { App } from '@capacitor/app';
import { OverlayBubble } from 'capacitor-overlay-bubble';

@Injectable({ providedIn: 'root' })
export class CallOverlayService {

  async initOverlayOnBackground(partnerName: string, avatarUrl: string) {
    // 1. Check & Request permission if needed
    const perm = await OverlayBubble.checkPermission();
    if (!perm.granted) {
      await OverlayBubble.requestPermission();
      return;
    }

    // 2. Listen to app background state
    App.addListener('appStateChange', async (state) => {
      if (!state.isActive) {
        await OverlayBubble.showBubble({
          calleeName: partnerName,
          avatarUrl: avatarUrl,
          callType: 'voice',
          durationSeconds: 0
        });
      } else {
        await OverlayBubble.hideBubble();
      }
    });

    // 3. Listen to bubble tap
    OverlayBubble.addListener('bubbleTapped', () => {
      OverlayBubble.hideBubble();
    });
  }
}
```

---

## License

[MIT](LICENSE)
