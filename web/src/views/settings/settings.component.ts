import {Component} from '@angular/core';
import {map, Observable} from 'rxjs';
import {Preference} from 'web/src/models/preference';
import {User} from 'web/src/models/user';
import {DataService} from 'web/src/services/data.service';
import {LoginService} from 'web/src/services/login.service';
import {UiService} from 'web/src/services/ui.service';

interface Settings {
  theme: string;
  allowNewUsers: boolean;
}

@Component({
  selector: 'app-settings',
  templateUrl: './settings.component.html',
  styleUrls: ['./settings.component.scss'],
})
/** Settings View component */
export class SettingsViewComponent {
  user$: Observable<User>;
  prefs$: Observable<Map<string, Preference>>;
  allThemes = this.ui.getAllThemes();

  model: Settings = {
    theme: 'light',
    allowNewUsers: true,
  };

  constructor(
      private readonly data: DataService,
      private readonly ui: UiService,
      private readonly login: LoginService,
  ) {
    this.user$ = this.login.getUserAsObservable();

    this.prefs$ = this.data.getPreferences().pipe(
        map((prefs) => {
          // Init form with current settings.
          if (prefs.has('theme')) {
            this.model.theme = prefs.get('theme')?.value;
          }
          if (prefs.has('allowNewUsers')) {
            // Parse string boolean to boolean
            this.model.allowNewUsers =
                prefs.get('allowNewUsers').value === 'true';
          }
          return prefs;
        }),
    );
  }

  /**
   * Handler for the theme change select.
   * @param newTheme
   */
  onThemeChange(newTheme: string): void {
    this.ui.setPageTheme(newTheme);
    this.data.setPreference({key: 'theme', value: newTheme}).subscribe();
  }

  /**
   * Handler for the allow new users toggle.
   * @param allowed
   */
  onAllowNewUsersChange(allowed: boolean) {
    const value = allowed ? 'true' : 'false';
    this.data.setPreference({key: 'allowNewUsers', value}).subscribe();
  }

  /**
   * Handler for a download all links request.
   */
  handleDownloadRequest(): void {
    this.data.getLinksWithNoFilters().subscribe((links) => {
      const blob = new Blob([JSON.stringify(links)]);
      const a = document.createElement('a');
      const objectUrl = URL.createObjectURL(blob);
      a.href = objectUrl;
      a.download = 'rook.json';
      a.click();
      URL.revokeObjectURL(objectUrl);
    });
  }
}
