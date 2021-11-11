import {Component} from '@angular/core';
import {Observable} from 'rxjs';
import {Link} from 'web/src/models/link';
import {DataService} from 'web/src/services/data.service';


@Component({
  selector: 'app-link-list',
  templateUrl: './linklist.component.html',
  styleUrls: ['./linklist.component.scss'],
})
export class LinkListComponent {
  links$: Observable<Link[]>;


  constructor(
      private readonly data: DataService,
  ) {
    this.links$ = this.data.getLinks();
  }
}
