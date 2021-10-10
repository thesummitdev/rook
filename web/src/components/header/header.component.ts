import {Component} from '@angular/core';
import {Observable} from 'rxjs';
import {User} from 'web/src/models/user';
import {CookieService} from 'web/src/services/cookie.service';
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
      private readonly cookie: CookieService,
  ) {
    this.user$ = this.login.getUserAsObservable();
  }
}
