import {
  animate,
  AnimationTriggerMetadata,
  state,
  style,
  transition,
  trigger,
} from '@angular/animations';

export const toastAnimations: {
  readonly toastState: AnimationTriggerMetadata;
} = {
  /** Animation that shows and hides a snack bar. */
  toastState: trigger('state', [
    state('void, hidden', style({ transform: 'scale(0.8)', opacity: 0 })),
    state('visible', style({ transform: 'scale(1)', opacity: 1 })),
    transition('* => visible', animate('350ms cubic-bezier(0, 0, 0.2, 1)')),
    transition(
      '* => void, * => hidden',
      animate('275ms cubic-bezier(0.4, 0.0, 1, 1)', style({ opacity: 0 }))
    ),
  ]),
};
