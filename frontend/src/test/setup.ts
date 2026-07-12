import { config } from '@vue/test-utils'
import { createVuetify } from 'vuetify'
import * as components from 'vuetify/components'
import * as directives from 'vuetify/directives'

const vuetify = createVuetify({ components, directives })

config.global.plugins.push(vuetify)
config.global.stubs = { transition: false, 'transition-group': false }

// jsdom doesn't implement visualViewport or ResizeObserver, which Vuetify's
// overlay positioning (VMenu, VDialog, etc.) reads on mount/unmount.
if (!window.visualViewport) {
  window.visualViewport = {
    width: window.innerWidth,
    height: window.innerHeight,
    offsetLeft: 0,
    offsetTop: 0,
    scale: 1,
    addEventListener: () => {},
    removeEventListener: () => {},
    dispatchEvent: () => true,
  } as unknown as VisualViewport
}

if (!window.ResizeObserver) {
  window.ResizeObserver = class ResizeObserver {
    observe(): void {}
    unobserve(): void {}
    disconnect(): void {}
  }
}
