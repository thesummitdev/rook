import {InjectionToken} from '@angular/core';
import {DialogContainer} from 'web/src/components/dialog/dialog.container.component';
import {ToastConfig} from 'web/src/components/toast/toast.config';

export const TOAST_CONFIG = new InjectionToken<ToastConfig>('TOAST_CONFIG');
export const DIALOG_CONTAINER =
    new InjectionToken<DialogContainer>('DIALOG_CONTAINER');
