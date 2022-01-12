import {Component} from '@angular/core';
import {Observable} from 'rxjs';
import {User} from 'web/src/models/user';
import {CookieService} from 'web/src/services/cookie.service';
import {DataService} from 'web/src/services/data.service';
import {LoginService} from 'web/src/services/login.service';
import {UiService} from 'web/src/services/ui.service';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss'],
})
/** Base level component */
export class AppComponent {
  user$: Observable<User|undefined>;
  readonly defaultTheme = 'light';

  constructor(
      private readonly cookie: CookieService,
      private readonly login: LoginService,
      private readonly ui: UiService,
      private readonly data: DataService,
  ) {
    this.user$ = this.login.getUserAsObservable();

    // Init page theme
    this.data.getPreferences().subscribe((prefs) => {
      this.ui.setPageTheme(prefs.get('theme')?.value || this.defaultTheme);
    });
  }
}
