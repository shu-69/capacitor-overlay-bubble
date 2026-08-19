import { WebPlugin } from '@capacitor/core';
export class OverlayBubbleWeb extends WebPlugin {
    async showBubble(_options) {
        console.warn('OverlayBubble is not supported on web platform.');
    }
    async hideBubble() {
        console.warn('OverlayBubble is not supported on web platform.');
    }
    async updateBubble(_options) {
        console.warn('OverlayBubble is not supported on web platform.');
    }
    async checkPermission() {
        return { granted: false };
    }
    async requestPermission() {
        return { granted: false };
    }
}
//# sourceMappingURL=web.js.map