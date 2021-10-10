import {Component} from '@angular/core';
import {Observable} from 'rxjs';
import {filter, map} from 'rxjs/operators';
import {CookieService} from 'web/src/services/cookie.service';
import {DataService} from 'web/src/services/data.service';
import {LoginService} from 'web/src/services/login.service';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss'],
})
/** Base level component */
export class AppComponent {
  username: Observable<string>;

  constructor(
      private readonly data: DataService,
      private readonly login: LoginService,
      private readonly cookie: CookieService,
  ) {
    this.username = this.login.getUserAsObservable().pipe(
        filter((user) => user.username !== undefined),
        map((user) => user.username));

    this.login.attemptSignIn('tyler', '12w3').subscribe(() => {
      this.data.getLinks().subscribe(console.log);
    });
  }
}
