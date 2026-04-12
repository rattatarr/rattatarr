import type { MenuItem } from 'primevue/menuitem'
import { Icon } from '@/utils/enums'

export const navigationItems: MenuItem[] = [
  {
    label: 'Dashboard',
    icon: Icon.HOME,
    route: '/',
  },
  // {
  //   label: 'Ratings',
  //   icon: Icon.STAR,
  //   route: '/ratings',
  // },
  {
    label: 'Movies',
    icon: Icon.FILM,
    route: '/movies',
  },
  {
    label: 'Series',
    icon: Icon.TH_LARGE,
    route: '/series',
  },
  {
    label: 'Broken',
    icon: Icon.FILE_EXCEL,
    route: '/broken',
  },
  {
    separator: true,
  },
  {
    label: 'Logs',
    icon: Icon.FILE,
    route: '/logs',
  },
  {
    label: 'Settings',
    icon: Icon.COG,
    route: '/settings',
  },
  {
    label: 'How To',
    icon: Icon.QUESTION_CIRCLE,
    route: '/how-to',
  },
]
