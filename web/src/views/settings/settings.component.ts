import { Component } from '@angular/core';
import { map, Observable, startWith, Subject, switchMap, take } from 'rxjs';
import { ApiKey } from 'web/src/models/apikey';
import { Preference } from 'web/src/models/preference';
import { User } from 'web/src/models/user';
import { DataService } from 'web/src/services/data.service';
import { DialogService } from 'web/src/services/dialog.service';
import { LoginService } from 'web/src/services/login.service';
import { ToastService } from 'web/src/services/toast.service';
import { UiService } from 'web/src/services/ui.service';

interface Settings {
  theme: string;
  allowNewUsers: boolean;
  pageSize: number;
}

@Component({
  selector: 'app-settings',
  templateUrl: './settings.component.html',
  styleUrls: ['./settings.component.scss'],
})
/** Settings View component */
export class SettingsViewComponent {
  user$: Observable<User>;
  apiKeys$: Observable<ApiKey[]>;
  refreshApiKeys$: Subject<void> = new Subject();
  readonly displayedColumns = ['agent', 'key', 'copy', 'delete'];
  prefs$: Observable<Map<string, Preference>>;
  allThemes = this.ui.getAllThemes();
  readonly pageSizes = [5, 20, 40, 75, 100];

  model: Settings = {
    theme: 'light',
    allowNewUsers: true,
    pageSize: 20,
  };

  constructor(
    private readonly data: DataService,
    private readonly ui: UiService,
    private readonly login: LoginService,
    private readonly toast: ToastService,
    private readonly dialog: DialogService
  ) {
    this.user$ = this.login.getUserAsObservable();
    this.apiKeys$ = this.refreshApiKeys$.pipe(
      startWith(null),
      switchMap(() => this.data.getApiKeys())
    );
    this.prefs$ = this.data.getPreferences().pipe(
      map(prefs => {
        // Init form with current settings.
        if (prefs.has('theme')) {
          this.model.theme = prefs.get('theme')?.value;
        }
        if (prefs.has('allowNewUsers')) {
          // Parse string boolean to boolean
          this.model.allowNewUsers =
            prefs.get('allowNewUsers').value === 'true';
        }
        if (prefs.has('pageSize')) {
          this.model.pageSize = Number(prefs.get('pageSize').value);
        }
        return prefs;
      })
    );
  }

  /**
   * Handler for the theme change select.
   *
   * @param {string} newTheme theme to select
   */
  onThemeChange(newTheme: string): void {
    this.ui.setPageTheme(newTheme);
    this.data.setPreference({ key: 'theme', value: newTheme }).subscribe();
  }

  onPageSizeChange(newSize: number): void {
    this.data
      .setPreference({ key: 'pageSize', value: newSize.toString() })
      .subscribe();
  }

  /**
   * Handler for the allow new users toggle.
   *
   * @param {boolean} allowed if new users can be created
   */
  onAllowNewUsersChange(allowed: boolean) {
    const value = allowed ? 'true' : 'false';
    this.data.setPreference({ key: 'allowNewUsers', value }).subscribe();
  }

  /**
   * Handler for a download all links request.
   */
  handleDownloadRequest(): void {
    this.data.getLinksWithNoFilters().subscribe(links => {
      const blob = new Blob([JSON.stringify(links)]);
      const a = document.createElement('a');
      const objectUrl = URL.createObjectURL(blob);
      a.href = objectUrl;
      a.download = 'rook.json';
      a.click();
      URL.revokeObjectURL(objectUrl);
    });
  }

  /**
   * Click handler for creating a new API key.
   *
   * @param {MouseEvent} event : the click event
   */
  handleCreateApiKey(event: MouseEvent): void {
    event.stopPropagation();
    this.data.createApiKey().subscribe(() => {
      this.refreshApiKeys$.next(null);
    });
  }

  /**
   * Click handler for deleting an API key.
   *
   * @param {MouseEvent} event : the click event
   * @param {ApiKey} key : the table row's api key.
   */
  handleDeleteApiKey(event: MouseEvent, key: ApiKey): void {
    event.stopPropagation();
    this.data.deleteApiKey(key).subscribe(() => {
      // re-fetch the list of ApiKeys.
      this.refreshApiKeys$.next(null);
    });
  }

  /**
   * Click handler for copying a key to the clipboard.
   *
   * @param {MouseEvent} event : the click event.
   * @param {ApiKey} key : the table row's api key.
   */
  handleCopyApiKey(event: MouseEvent, key: ApiKey): void {
    event.stopPropagation();
    navigator.clipboard.writeText(key.apiKey).then(
      // Success
      () => {
        this.toast.showMessage('Copied!');
      },
      // Error
      () => {
        this.toast.showError('Something bad happened.');
      }
    );
  }

  /**
   * Click handler for showing a key in a dialog.
   *
   * @param {MouseEvent} event : the click event.
   * @param {ApiKey} key : the table row's api key.
   */
  handleShowApiKey(event: MouseEvent, key: ApiKey) {
    event.stopPropagation();
    this.dialog
      .showApiKeyDialog(key)
      .resultAsObservable()
      .pipe(take(1))
      .subscribe(() => {
        // re-fetch list since it might have been deleted.
        this.refreshApiKeys$.next(null);
      });
  }
}
