import {Component} from '@angular/core';
import {Observable} from 'rxjs';
import {map} from 'rxjs/operators';
import {DataService} from 'web/src/services/data.service';

@Component({
  selector: 'app-filter-panel',
  templateUrl: './filterpanel.component.html',
  styleUrls: ['./filterpanel.component.scss'],
})
export class FilterPanelComponent {
  tags$: Observable<Set<string>>;

  constructor(private readonly data: DataService) {
    // TODO: Don't fetch links twice, this should use a tag specific endpoint
    // which handles the dedupe and sort on the server.
    this.tags$ = this.data.getLinks().pipe(map((links) => {
      const allTags = links.map((el) => el.tags.split(' ')).flat().sort();
      return new Set(allTags);
    }));
  }
}
