import {Component} from '@angular/core';
import {Observable} from 'rxjs';
import {User} from 'web/src/models/user';
import {LoginService} from 'web/src/services/login.service';

@Component({
  selector: 'app-root',
  templateUrl: './main.component.html',
  styleUrls: ['./main.component.scss'],
})
/** Base level component */
export class MainComponent {
  user$: Observable<User|undefined>;

  constructor(
      private readonly login: LoginService,
  ) {
    this.user$ = this.login.getUserAsObservable();
  }
}
