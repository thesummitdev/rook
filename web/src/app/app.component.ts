import {Component} from '@angular/core';
import {DataService} from 'web/src/services/data.service';

@Component({
  selector : 'app-root',
  templateUrl : './app.component.html',
  styleUrls : [ './app.component.scss' ]
})
export class AppComponent {
  title = 'frontend';

  constructor(private readonly data: DataService) {}
}
