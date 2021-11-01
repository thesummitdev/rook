import {Component} from '@angular/core';
import {Observable} from 'rxjs';
import {User} from 'web/src/models/user';
import {DialogService} from 'web/src/services/dialog.service';
import {LoginService} from 'web/src/services/login.service';


@Component({
  selector: 'app-header',
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.scss'],
})
export class HeaderComponent {
  user$: Observable<User|undefined>;


  constructor(
      private readonly login: LoginService,
      private readonly dialog: DialogService,
  ) {
    this.user$ = this.login.getUserAsObservable();
  }

  showLogin(event: PointerEvent): void {
    event.stopPropagation();
    this.dialog.showLoginDialog().resultAsObservable().subscribe((result) => {
      if (!result.cancelled) {
        const {username, password} = result.result;
        this.login.attemptSignIn(username, password).subscribe(console.log);
      }
    });
  }

  signOut(): void {
    this.login.signOut();
  }
}
