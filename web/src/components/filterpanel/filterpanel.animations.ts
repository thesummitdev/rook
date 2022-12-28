import {
  animate,
  AnimationTriggerMetadata,
  state,
  style,
  transition,
  trigger,
} from '@angular/animations';

export const filterPanelAnimations: {
  readonly growHeight: AnimationTriggerMetadata;
} = {
  growHeight: trigger('growHeight', [
    state('inactive', style({ height: '0px', overflow: 'hidden' })),
    state('active', style({ height: '*', overflow: 'hidden' })),
    transition('inactive => active', animate('200ms ease-in')),
    transition('active => inactive', animate('200ms ease-in')),
  ]),
};
