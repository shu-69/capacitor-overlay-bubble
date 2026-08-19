import { WebPlugin } from '@capacitor/core';
import type { OverlayBubblePlugin, ShowBubbleOptions, UpdateBubbleOptions, OverlayBubblePermissionStatus } from './definitions';
export declare class OverlayBubbleWeb extends WebPlugin implements OverlayBubblePlugin {
    showBubble(_options?: ShowBubbleOptions): Promise<void>;
    hideBubble(): Promise<void>;
    updateBubble(_options?: UpdateBubbleOptions): Promise<void>;
    checkPermission(): Promise<OverlayBubblePermissionStatus>;
    requestPermission(): Promise<OverlayBubblePermissionStatus>;
}
