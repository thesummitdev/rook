import { Component } from '@angular/core';
import { combineLatest, Observable, of, startWith, switchMap } from 'rxjs';
import { Link } from 'web/src/models/link';
import { DataService } from 'web/src/services/data.service';
import { FilterService } from 'web/src/services/filters.service';
import { LoginService } from 'web/src/services/login.service';

@Component({
  selector: 'app-link-list',
  templateUrl: './linklist.component.html',
  styleUrls: ['./linklist.component.scss'],
})
/** A component that accepts application state and renders a list of links. */
export class LinkListComponent {
  links$: Observable<Link[] | null>;

  constructor(
    private readonly data: DataService,
    private readonly filters: FilterService,
    private readonly login: LoginService
  ) {
    // Pay attention to the current user status, and if the user is logged in,
    // immediately fetch the corresponding set of links, given the filters that
    // are currently set.
    this.links$ = combineLatest([
      this.login.getUserAsObservable(),
      // Refresh the list when a new link is added.
      // TODO: Consider a way to amend the new link to the existing list,
      // rather than refetching the entire list and doing an expensive DOM
      // update, but I guess this is simple enough for now and works.
      this.data.getNewLinksAsObservable().pipe(startWith(null)),
    ]).pipe(
      switchMap(([user, _]) => {
        if (user) {
          return this.filters
            .getTagsAsObservable()
            .pipe(switchMap(() => this.data.getLinks()));
        }

        // If there is no user, then display the null state.
        return of(null);
      })
    );
  }
}
