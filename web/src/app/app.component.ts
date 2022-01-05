import {Component} from '@angular/core';
import {CookieService} from 'web/src/services/cookie.service';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss'],
})
/** Base level component */
export class AppComponent {
  constructor(
      private readonly cookie: CookieService,
  ) {}
}
