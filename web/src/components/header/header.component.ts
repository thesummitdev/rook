import {Component} from '@angular/core';
import {EMPTY, Observable} from 'rxjs';
import {map, shareReplay, switchMap, take} from 'rxjs/operators';
import {User} from 'web/src/models/user';
import {DataService} from 'web/src/services/data.service';
import {DialogService} from 'web/src/services/dialog.service';
import {LoginService} from 'web/src/services/login.service';
import {UiService} from 'web/src/services/ui.service';


@Component({
  selector: 'app-header',
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.scss'],
})
export class HeaderComponent {
  user$: Observable<User|undefined>;
  allowCreateNewUsers$: Observable<boolean>;

  constructor(
      private readonly login: LoginService,
      private readonly dialog: DialogService,
      private readonly ui: UiService,
      private readonly data: DataService,
  ) {
    this.user$ = this.login.getUserAsObservable().pipe(
        shareReplay(),
    );
    this.allowCreateNewUsers$ = this.data.getPreferences().pipe(
        map((prefs) => prefs.get('allowNewUsers')?.value === 'true'),
    );
  }

  showCreateAccount(event: MouseEvent): void {
    event.stopPropagation();
    this.dialog.showCreateAccountDialog()
        .resultAsObservable()
        .pipe(
            switchMap((dialog) => {
              if (!dialog.cancelled) {
                const {username, password} = dialog.result;
                return this.data.createUser({username, password})
                    .pipe(
                        switchMap(
                            (user) =>
                                this.login.attemptSignIn(username, password)),
                    );
              }
              return EMPTY;
            }),
            take(1))
        .subscribe();
  }

  showLogin(event: MouseEvent): void {
    event.stopPropagation();
    this.dialog.showLoginDialog().resultAsObservable().subscribe((result) => {
      if (!result.cancelled) {
        const {username, password} = result.result;
        this.login.attemptSignIn(username, password).subscribe();
      }
    });
  }

  showCreate(event: MouseEvent): void {
    event.stopPropagation();
    const dialog = this.dialog.showAddLinkDialog();
    dialog.resultAsObservable().pipe(take(1)).subscribe((dialog) => {
      // If the dialog includes a result, then add the link to the backend.
      if (dialog.result) {
        this.data.createLink(dialog.result).subscribe();
      }
    });
  }

  signOut(): void {
    this.login.signOut();
  }
}
