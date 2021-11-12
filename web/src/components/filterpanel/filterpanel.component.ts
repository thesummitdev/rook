import {Component} from '@angular/core';
import {Observable} from 'rxjs';
import {DataService} from 'web/src/services/data.service';

@Component({
  selector: 'app-filter-panel',
  templateUrl: './filterpanel.component.html',
  styleUrls: ['./filterpanel.component.scss'],
})
export class FilterPanelComponent {
  tags$: Observable<String[]>;

  constructor(private readonly data: DataService) {
    // TODO: Don't fetch links twice, this should use a tag specific endpoint
    // which handles the dedupe and sort on the server.
    this.tags$ = this.data.getTags();
  }
}
