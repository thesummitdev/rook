import {Component} from '@angular/core';
import {Observable} from 'rxjs';
import {startWith, switchMap, take} from 'rxjs/operators';
import {DataService} from 'web/src/services/data.service';
import {FilterService} from 'web/src/services/filters.service';
import {UiService} from 'web/src/services/ui.service';

import {filterPanelAnimations} from './filterpanel.animations';

@Component({
  selector: 'app-filter-panel',
  templateUrl: './filterpanel.component.html',
  styleUrls: ['./filterpanel.component.scss'],
  animations: [filterPanelAnimations.growHeight],
})
/** FilterPanel in the left hand column. */
export class FilterPanelComponent {
  tags$: Observable<string[]>;
  selectedTags$: Observable<Set<string>>;
  readonly show: Observable<boolean>;

  constructor(
      private readonly data: DataService,
      private readonly filters: FilterService,
      private readonly ui: UiService,
  ) {
    this.show = this.ui.getFilterPanelAsObservable();
    this.tags$ = this.data.getNewLinksAsObservable().pipe(
        startWith(null),
        switchMap(() => this.data.getTags()),
    );

    this.selectedTags$ = this.filters.getTagsAsObservable();
  }

  onSearchChange(searchTerm: string) {
    this.filters.setSearch(searchTerm || undefined);
  }

  /**
   * When a tag is added to the selected tags from the tag dropdown.
   * This pushes the new list of tags to the FilterService.
   * @param tags - the currently selected tags.
   */
  onTagSelected(tags: string[]): void {
    this.filters.setTags(tags);
  }

  /**
   * When a tag is removed from the currently selected list of tags.
   * This removes the tag from the set and pushes the new list to the
   * FilterService.
   * @param tag - the tag to remove
   */
  onTagRemoved(tag: string): void {
    this.filters.getTagsAsObservable().pipe(take(1)).subscribe((tags) => {
      tags.delete(tag);
      this.filters.setTags(tags);
    });
  }
}
