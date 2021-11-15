import {Component} from '@angular/core';
import {Observable} from 'rxjs';
import {switchMap} from 'rxjs/operators';
import {Link} from 'web/src/models/link';
import {DataService} from 'web/src/services/data.service';
import {FilterService} from 'web/src/services/filters.service';


@Component({
  selector: 'app-link-list',
  templateUrl: './linklist.component.html',
  styleUrls: ['./linklist.component.scss'],
})
export class LinkListComponent {
  links$: Observable<Link[]>;


  constructor(
      private readonly data: DataService,
      private readonly filters: FilterService,
  ) {
    this.links$ = this.filters.getTagsAsObservable().pipe(
        switchMap(() => this.data.getFilteredLinks()));
  }
}
