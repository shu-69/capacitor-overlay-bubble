import type { PluginListenerHandle } from '@capacitor/core';
export interface ShowBubbleOptions {
    avatarUrl?: string;
    calleeName?: string;
    callType?: 'video' | 'voice';
    durationSeconds?: number;
}
export interface UpdateBubbleOptions {
    durationSeconds?: number;
    avatarUrl?: string;
    calleeName?: string;
}
export interface OverlayBubblePermissionStatus {
    granted: boolean;
}
export interface OverlayBubblePlugin {
    showBubble(options?: ShowBubbleOptions): Promise<void>;
    hideBubble(): Promise<void>;
    updateBubble(options?: UpdateBubbleOptions): Promise<void>;
    checkPermission(): Promise<OverlayBubblePermissionStatus>;
    requestPermission(): Promise<OverlayBubblePermissionStatus>;
    addListener(eventName: 'bubbleTapped', listenerFunc: () => void): Promise<PluginListenerHandle>;
    addListener(eventName: 'bubbleDismissed', listenerFunc: () => void): Promise<PluginListenerHandle>;
    removeAllListeners(): Promise<void>;
}
