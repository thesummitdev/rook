import {Component, Inject} from '@angular/core';
import {TOAST_CONFIG} from 'web/src/util/injectiontokens';
import {ToastConfig} from './toast.config';

@Component({
  templateUrl: 'toast.component.html',
  styleUrls: ['toast.component.scss'],
})
export class ToastComponent {
  constructor(
      @Inject(TOAST_CONFIG) public config: ToastConfig,
  ) {}
}
