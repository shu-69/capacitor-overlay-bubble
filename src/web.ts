import { WebPlugin } from '@capacitor/core';
import type {
  OverlayBubblePlugin,
  ShowBubbleOptions,
  UpdateBubbleOptions,
  OverlayBubblePermissionStatus,
} from './definitions';

export class OverlayBubbleWeb extends WebPlugin implements OverlayBubblePlugin {
  async showBubble(_options?: ShowBubbleOptions): Promise<void> {
    console.warn('OverlayBubble is not supported on web platform.');
  }

  async hideBubble(): Promise<void> {
    console.warn('OverlayBubble is not supported on web platform.');
  }

  async updateBubble(_options?: UpdateBubbleOptions): Promise<void> {
    console.warn('OverlayBubble is not supported on web platform.');
  }

  async checkPermission(): Promise<OverlayBubblePermissionStatus> {
    return { granted: false };
  }

  async requestPermission(): Promise<OverlayBubblePermissionStatus> {
    return { granted: false };
  }
}
