import {Component} from '@angular/core';
import {Observable} from 'rxjs';
import {User} from 'web/src/models/user';
import {CookieService} from 'web/src/services/cookie.service';
import {LoginService} from 'web/src/services/login.service';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss'],
})
/** Base level component */
export class AppComponent {
  user$: Observable<User|undefined>;

  constructor(
      private readonly cookie: CookieService,
      private readonly login: LoginService,
  ) {
    this.user$ = this.login.getUserAsObservable();
  }
}
