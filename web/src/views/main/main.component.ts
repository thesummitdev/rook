import {Component, OnDestroy, OnInit} from '@angular/core';
import {Observable} from 'rxjs';
import {User} from 'web/src/models/user';
import {LoginService} from 'web/src/services/login.service';
import {UiService} from 'web/src/services/ui.service';

@Component({
  selector: 'app-main',
  templateUrl: './main.component.html',
  styleUrls: ['./main.component.scss'],
})
/** Main view component */
export class MainViewComponent implements OnInit, OnDestroy {
  user$: Observable<User|undefined>;

  constructor(
      private readonly login: LoginService,
      private readonly ui: UiService,
  ) {
    this.user$ = this.login.getUserAsObservable();
  }
  ngOnInit(): void {
    this.ui.setFilterPanelVisible(true);
  }
  ngOnDestroy(): void {
    this.ui.setFilterPanelVisible(false);
  }
}
