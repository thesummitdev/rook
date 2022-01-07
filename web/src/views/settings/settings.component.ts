import {Component} from '@angular/core';
import {Observable} from 'rxjs';
import {Preference} from 'web/src/models/preference';
import {DataService} from 'web/src/services/data.service';

@Component({
  selector: 'app-settings',
  templateUrl: './settings.component.html',
  styleUrls: ['./settings.component.scss'],
})
/** Settings View component */
export class SettingsViewComponent {
  prefs$: Observable<Map<string, Preference>>;

  constructor(private readonly data: DataService) {
    this.prefs$ = this.data.getPreferences();
  }
}
