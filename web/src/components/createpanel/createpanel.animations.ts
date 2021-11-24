import {animate, AnimationTriggerMetadata, state, style, transition, trigger} from '@angular/animations';


export const createPanelAnimations: {
  readonly growHeight: AnimationTriggerMetadata,
  readonly formErrorMessage: AnimationTriggerMetadata,
} = {
  growHeight: trigger(
      'growHeight',
      [
        state('inactive', style({height: '0'})),
        state('active', style({height: '*'})),
        transition('inactive => active', animate('200ms ease-in')),
        transition('active => inactive', animate('200ms ease-in')),
      ]),
      formErrorMessage: trigger('formErrorMessage', [
        transition(':enter', [style({opacity: 0}), animate('200ms ease-in', style({opacity: 1})),]),
        transition(':leave', [style({opacity: 1}), animate('200ms ease-in', style({opacity: 0})),]),
      ]),
};
