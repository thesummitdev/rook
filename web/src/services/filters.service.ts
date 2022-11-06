import { Injectable } from '@angular/core';
import { Observable, ReplaySubject, Subject } from 'rxjs';

@Injectable({ providedIn: 'root' })
/** Service that provides access to filter settings */
export class FilterService {
  private tags$ = new ReplaySubject<Set<string>>(1);
  private searchTerm$ = new ReplaySubject<string | undefined>(1);
  private cursor$: Subject<string | undefined> = new Subject();

  constructor() {
    this.tags$.next(new Set([]));
    this.searchTerm$.next(undefined);
  }

  /**
   * Sets the current tags to be used for data requests.
   *
   * @param nextTags
   */
  setTags(nextTags: string[] | Set<string>): void {
    this.cursor$.next(undefined);
    if (Array.isArray(nextTags)) {
      this.tags$.next(new Set(nextTags));
    } else {
      this.tags$.next(nextTags);
    }
  }

  setCursor(cursor?: string) {
    this.cursor$.next(cursor);
  }

  getCursorAsObservable(): Observable<string | undefined> {
    return this.cursor$.asObservable();
  }

  /**
   * Sets the current search term to be used for data requests.
   *
   * @param search
   */
  setSearch(search: string | undefined): void {
    this.searchTerm$.next(search);
  }

  /**
   * Returns the current set of tags as an observable.
   *
   * @returns Observable of the current tags.
   */
  getTagsAsObservable(): Observable<Set<string>> {
    return this.tags$.asObservable();
  }

  /**
   * Returns the current search term as an observable.
   *
   * @returns Observable of the current search term.
   */
  getSearchAsObservable(): Observable<string | undefined> {
    return this.searchTerm$.asObservable();
  }
}
