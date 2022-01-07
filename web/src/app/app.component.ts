import {Component} from '@angular/core';
import {Observable} from 'rxjs';
import {CookieService} from 'web/src/services/cookie.service';

import {User} from '../models/user';
import {LoginService} from '../services/login.service';

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
