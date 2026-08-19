import { registerPlugin } from '@capacitor/core';
import type { OverlayBubblePlugin } from './definitions';

const OverlayBubble = registerPlugin<OverlayBubblePlugin>('OverlayBubble', {
  web: () => import('./web').then(m => new m.OverlayBubbleWeb()),
});

export * from './definitions';
export { OverlayBubble };
