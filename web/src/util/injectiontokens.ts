import {InjectionToken} from '@angular/core';
import {ToastConfig} from 'web/src/components/toast/toast.config';

export const TOAST_CONFIG = new InjectionToken<ToastConfig>('TOAST_CONFIG');
