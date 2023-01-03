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
   * Sets the current list/set of tags to be used for data requests.
   *
   * @param {Set<string>|string[]} nextTags the list of selected tags. will be
   *                                        converted into a set if not already
   *                                        a set.
   */
  setTags(nextTags: string[] | Set<string>): void {
    this.cursor$.next(undefined);
    if (Array.isArray(nextTags)) {
      this.tags$.next(new Set(nextTags));
    } else {
      this.tags$.next(nextTags);
    }
  }

  /**
   * Sets the current pagination cursor to be used for data requests.
   *
   * @param {string|undefined} cursor the next cursor to use. Can be undefined
   *                                  to force the first page to be fetched.
   */
  setCursor(cursor?: string): void {
    this.cursor$.next(cursor);
  }

  /**
   * Sets the current search term to be used for data requests.
   *
   * @param {string|undefined} search the search term to use for requests.
   */
  setSearch(search: string | undefined): void {
    this.searchTerm$.next(search);
  }

  /**
   * Returns the current set of tags as an observable.
   *
   * @returns {Observable<Set<string>>} stream of the current tags.
   */
  getTagsAsObservable(): Observable<Set<string>> {
    return this.tags$.asObservable();
  }

  /**
   * Get an observable stream of the current cursor.
   *
   * @returns {Observable<string|undefined>} stream of cursor
   */
  getCursorAsObservable(): Observable<string | undefined> {
    return this.cursor$.asObservable();
  }

  /**
   * Returns the current search term as an observable.
   *
   * @returns {Observable<string|undefined>} stream of the current search term.
   */
  getSearchAsObservable(): Observable<string | undefined> {
    return this.searchTerm$.asObservable();
  }
}
