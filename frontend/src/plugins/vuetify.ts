import { createVuetify } from 'vuetify'
import * as components from 'vuetify/components'
import * as directives from 'vuetify/directives'
import 'vuetify/styles'

export default createVuetify({
  components,
  directives,
  theme: {
    defaultTheme: 'light',
    themes: {
      light: {
        colors: {
          primary: '#1565C0',
          secondary: '#0288D1',
          success: '#43A047',
          warning: '#FB8C00',
          error: '#E53935',
          info: '#039BE5',
          surface: '#FFFFFF',
          background: '#F5F7FA',
        },
      },
      dark: {
        colors: {
          primary: '#42A5F5',
          secondary: '#29B6F6',
          success: '#66BB6A',
          warning: '#FFA726',
          error: '#EF5350',
          info: '#29B6F6',
          surface: '#1E1E2E',
          background: '#13131F',
        },
      },
    },
  },
})
