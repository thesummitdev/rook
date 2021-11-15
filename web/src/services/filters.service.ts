import {Injectable} from '@angular/core';
import {Observable, ReplaySubject} from 'rxjs';


@Injectable({providedIn: 'root'})
/** Service that provides access to filter settings */
export class FilterService {
  private tags$ = new ReplaySubject<Set<string>>(1);
  private searchTerm$ = new ReplaySubject<string|undefined>(1);

  constructor() {
    this.tags$.next(new Set([]));
    this.searchTerm$.next(undefined);
  }

  /**
   * Sets the current tags to be used for data requests.
   * @param nextTags
   */
  setTags(nextTags: string[]|Set<string>): void {
    if (Array.isArray(nextTags)) {
      this.tags$.next(new Set(nextTags));
    } else {
      this.tags$.next(nextTags);
    }
  }

  /**
   * Returns the current set of tags as an observable.
   * @return Observable of the current tags.
   */
  getTagsAsObservable(): Observable<Set<string>> {
    return this.tags$.asObservable();
  }
}
