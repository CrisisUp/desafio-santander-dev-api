import { animate, query, style, transition, trigger } from '@angular/animations';

/**
 * Route-level transition: the incoming view fades in while sliding up slightly.
 * Used by the App shell around the <router-outlet>.
 */
export const routeAnimations = trigger('routeAnimations', [
  transition('* => *', [
    query(':enter', style({ opacity: 0, transform: 'translateY(12px)' }), { optional: true }),
    query(
      ':enter',
      animate('220ms ease-out', style({ opacity: 1, transform: 'translateY(0)' })),
      { optional: true }
    ),
  ]),
]);
