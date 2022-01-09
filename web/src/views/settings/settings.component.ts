import {Component} from '@angular/core';
import {map, Observable} from 'rxjs';
import {Preference} from 'web/src/models/preference';
import {DataService} from 'web/src/services/data.service';
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
  prefs$: Observable<Map<string, Preference>>;
  allThemes = this.ui.getAllThemes();

  model: Settings = {
    theme: 'light',
    allowNewUsers: true,
  };

  constructor(
      private readonly data: DataService,
      private readonly ui: UiService,
  ) {
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

  onAllowNewUsersChange(allowed: boolean) {
    const value = allowed ? 'true' : 'false';
    this.data.setPreference({key: 'allowNewUsers', value}).subscribe();
  }
}
