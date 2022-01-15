import {animate, AnimationTriggerMetadata, style, transition, trigger} from '@angular/animations';


export const linkFormAnimations: {
  readonly formErrorMessage: AnimationTriggerMetadata,
} = {
  formErrorMessage: trigger(
      'formErrorMessage',
      [
        transition(
            ':enter',
            [
              style({opacity: 0}),
              animate('200ms ease-in', style({opacity: 1})),
            ]),
        transition(
            ':leave',
            [
              style({opacity: 1}),
              animate('200ms ease-in', style({opacity: 0})),
            ]),
      ]),
};
