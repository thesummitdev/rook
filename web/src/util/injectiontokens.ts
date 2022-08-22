import { InjectionToken } from '@angular/core';
import { DialogContainer } from 'web/src/components/dialog/dialog.container.component';
import { ToastConfig } from 'web/src/components/toast/toast.config';
import { Link } from 'web/src/models/link';

export const TOAST_CONFIG = new InjectionToken<ToastConfig>('TOAST_CONFIG');
export const DIALOG_CONTAINER =
  new InjectionToken<DialogContainer>('DIALOG_CONTAINER');
export const LINK = new InjectionToken<Link>('LINK');
export const EDIT_MODE = new InjectionToken<boolean>('EDIT_MODE');
