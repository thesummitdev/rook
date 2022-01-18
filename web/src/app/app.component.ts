import {Component, OnDestroy} from '@angular/core';
import {Observable, ReplaySubject, takeUntil} from 'rxjs';
import {User} from 'web/src/models/user';
import {CookieService} from 'web/src/services/cookie.service';
import {DataService} from 'web/src/services/data.service';
import {HotkeysService} from 'web/src/services/hotkeys.service';
import {LoginService} from 'web/src/services/login.service';
import {UiService} from 'web/src/services/ui.service';

import {DialogService} from '../services/dialog.service';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss'],
})
/** Base level component */
export class AppComponent implements OnDestroy {
  private readonly destroyed$: ReplaySubject<void> = new ReplaySubject();
  user$: Observable<User|undefined>;
  readonly defaultTheme = 'light';

  constructor(
      private readonly cookie: CookieService,
      private readonly login: LoginService,
      private readonly ui: UiService,
      private readonly data: DataService,
      private readonly hotkeys: HotkeysService,
      private readonly dialog: DialogService,
  ) {
    this.user$ = this.login.getUserAsObservable();

    // Init page theme
    this.data.getPreferences().subscribe((prefs) => {
      this.ui.setPageTheme(prefs.get('theme')?.value || this.defaultTheme);
    });

    // Setup hotkeys
    this.hotkeys.addShortcut({keys: 'shift.+'})
        .pipe(takeUntil(this.destroyed$))
        .subscribe((event) => {
          this.dialog.showAddLinkDialog().resultAsObservable().subscribe(
              (dialog) => {
                if (dialog.result) {
                  this.data.createLink(dialog.result).subscribe();
                }
              });
        });
  }
  ngOnDestroy(): void {
    this.destroyed$.next();
  }
}
