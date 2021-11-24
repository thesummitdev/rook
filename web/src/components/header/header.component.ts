import {Component} from '@angular/core';
import {Observable} from 'rxjs';
import {shareReplay} from 'rxjs/operators';
import {User} from 'web/src/models/user';
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
  createPanelVisible$: Observable<boolean>;

  constructor(
      private readonly login: LoginService,
      private readonly dialog: DialogService,
      private readonly ui: UiService,
  ) {
    this.user$ = this.login.getUserAsObservable().pipe(
        shareReplay(),
    );
    this.createPanelVisible$ = this.ui.getCreatePanelAsObservable();
  }

  showLogin(event: MouseEvent): void {
    event.stopPropagation();
    this.dialog.showLoginDialog().resultAsObservable().subscribe((result) => {
      if (!result.cancelled) {
        const {username, password} = result.result;
        this.login.attemptSignIn(username, password).subscribe(console.log);
      }
    });
  }

  showCreate(event: MouseEvent): void {
    event.stopPropagation();
    this.ui.setCreatePanelVisible(true);
  }

  signOut(): void {
    this.login.signOut();
  }
}
