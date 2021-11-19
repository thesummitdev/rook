import {Component} from '@angular/core';
import {Observable, of} from 'rxjs';
import {switchMap} from 'rxjs/operators';
import {Link} from 'web/src/models/link';
import {DataService} from 'web/src/services/data.service';
import {FilterService} from 'web/src/services/filters.service';
import {LoginService} from 'web/src/services/login.service';


@Component({
  selector: 'app-link-list',
  templateUrl: './linklist.component.html',
  styleUrls: ['./linklist.component.scss'],
})
/** A component that accepts application state and renders a list of links. */
export class LinkListComponent {
  links$: Observable<Link[]|null>;


  constructor(
      private readonly data: DataService,
      private readonly filters: FilterService,
      private readonly login: LoginService,
  ) {
    // Pay attention to the current user status, and if the user is logged in,
    // immediately fetch the corresponding set of links, given the filters that
    // are currently set.
    this.links$ = this.login.getUserAsObservable().pipe(
        switchMap((user) => {
          if (user) {
            return this.filters.getTagsAsObservable().pipe(
                switchMap(() => this.data.getLinks()));
          }

          // If there is no user, then display the null state.
          return of(null);
        }),
    );
  }
}
