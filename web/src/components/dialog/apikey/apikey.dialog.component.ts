import { Component, Inject } from '@angular/core';
import { ApiKey } from 'web/src/models/apikey';
import { DataService } from 'web/src/services/data.service';
import { ToastService } from 'web/src/services/toast.service';
import { API_KEY, DIALOG_CONTAINER } from 'web/src/util/injectiontokens';

import { DialogComponent } from '../dialog.component';
import { DialogContainer } from '../dialog.container.component';

@Component({
  templateUrl: 'apikey.dialog.component.html',
  styleUrls: ['apikey.dialog.component.scss'],
})
export class ApiKeyDialogComponent extends DialogComponent<void> {
  readonly apikey: ApiKey;

  constructor(
    @Inject(DIALOG_CONTAINER) container: DialogContainer,
    @Inject(API_KEY) apikey: ApiKey,
    private readonly toast: ToastService,
    private readonly data: DataService
  ) {
    super(container);
    this.apikey = apikey;
  }

  /**
   * Writes the apikey to the clipboard.
   *
   * @param {MouseEvent} event - the click event
   */
  handleCopyKeyToClipboard(event: MouseEvent): void {
    event.stopPropagation();
    navigator.clipboard.writeText(this.apikey.apiKey).then(() => {
      this.toast.showMessage('Copied!');
    });
    this.close();
  }

  /**
   *
   * @param event
   */
  handleDeleteKey(event: MouseEvent): void {
    event.stopPropagation();
    this.data.deleteApiKey(this.apikey).subscribe(() => {
      this.setResult({ cancelled: false });
      this.close();
    });
  }
}
