import {Observable} from 'rxjs';

import {DialogResult} from './dialog.result';

/** Required methods and properties on Dialogs */
export interface DialogComponent {
  resultAsObservable: () => Observable<DialogResult<unknown>>;
  close: () => void;
  cancel: () => void;
}
