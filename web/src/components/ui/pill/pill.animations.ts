import {
  animate,
  AnimationTriggerMetadata,
  state,
  style,
  transition,
  trigger,
} from '@angular/animations';

export const pillAnimations: {
  readonly growWidth: AnimationTriggerMetadata;
  readonly fadeInOut: AnimationTriggerMetadata;
} = {
  growWidth: trigger('growWidth', [
    state('nh', style({ width: '0px' })),
    state('h', style({ width: '*' })),
    transition('nh <=> h', animate('200ms ease-in')),
  ]),
  fadeInOut: trigger('fadeInOut', [
    transition(':enter', [
      style({ opacity: 0 }),
      animate('100ms', style({ opacity: 1 })),
    ]),
    transition(':leave', [animate('100ms', style({ opacity: 0 }))]),
  ]),
};
