import { registerPlugin } from '@capacitor/core';
const OverlayBubble = registerPlugin('OverlayBubble', {
    web: () => import('./web').then(m => new m.OverlayBubbleWeb()),
});
export * from './definitions';
export { OverlayBubble };
//# sourceMappingURL=index.js.map