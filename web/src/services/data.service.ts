import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import {
  catchError,
  combineLatest,
  EMPTY,
  Observable,
  of,
  Subject,
} from 'rxjs';
import {
  debounceTime,
  map,
  shareReplay,
  startWith,
  switchMap,
} from 'rxjs/operators';
import { Link } from 'web/src/models/link';
import { Preference } from 'web/src/models/preference';
import { FilterService } from 'web/src/services/filters.service';
import { LoginService } from 'web/src/services/login.service';
import { ApiKey } from '../models/apikey';

import { User } from '../models/user';

import { ToastService } from './toast.service';

interface PagedResults<T> {
  cursor?: { next?: string; prev?: string };
  items: T[];
}

@Injectable({ providedIn: 'root' })
/** Data service that fetches data from the rook api. */
export class DataService {
  // Emit any newly created links individually so that components don't have
  // to refresh the entire list when a new link is added.
  private readonly newLinks$: Subject<Link | null> = new Subject();
  private readonly newPref$: Subject<void> = new Subject();
  private readonly preferences$: Observable<Map<string, Preference>>;

  constructor(
    private readonly http: HttpClient,
    private readonly filters: FilterService,
    private readonly login: LoginService,
    private readonly toast: ToastService
  ) {
    // Setup the preferences observable. This observable will keep the prefs
    // up-to-date for login / logout actions.
    this.preferences$ = combineLatest([
      this.login.getUserAsObservable(),
      this.newPref$.pipe(startWith(null)),
    ]).pipe(
      switchMap(() =>
        this.http.get<Preference[]>('/prefs').pipe(
          map(prefs => {
            const map = new Map<string, Preference>();
            for (const pref of prefs) {
              map.set(pref.key, pref);
            }
            return map;
          })
        )
      ),
      shareReplay(1)
    );
  }

  /**
   * Fetches the links from the backend, and includes applied filters as request
   * parameters.
   * NOTE: this requires the user to be logged in.
   * TODO: handle calls that don't have a logged in user, instead of erroring.
   *
   * @param   {string} cursor optional cursor to designate the fetch position.
   * @param   {number} size   optional page size to return.
   * @returns {Observable}    of the list of returned links. Can be empty an
   *                          empty list.
   */
  getLinks(cursor?: string, size?: number): Observable<PagedResults<Link>> {
    return combineLatest([
      this.filters.getSearchAsObservable().pipe(
        debounceTime(300) // debounce user input.
      ),
      this.filters.getTagsAsObservable(),
    ]).pipe(
      switchMap(([searchTerm, tags]) =>
        this.http.request<PagedResults<Link>>('POST', '/links', {
          body: JSON.stringify({
            cursor,
            title: searchTerm,
            tags: [...tags].join(' '),
            limit: size,
          }),
        })
      ),
      // If the route errors or there is no auth token present, just
      // return an empty array.
      catchError(() => of({ items: [] }))
    );
  }

  /**
   * Fetches the users current api keys.
   *
   * @returns {Observable} of the list of returned apikeys. Can be an empty
   *                       list.
   */
  getApiKeys(): Observable<ApiKey[]> {
    return this.http.get<ApiKey[]>('/users/apikey');
  }

  /**
   * Requests a new ApiKey from the Rook server.
   *
   * @returns {Observable} of the new ApiKey.
   */
  createApiKey(): Observable<ApiKey> {
    return this.http.get<ApiKey>('/users/apikey/new');
  }

  /**
   * Requests deletion of the requested ApiKey.
   *
   * @param {ApiKey} key The key to delete.
   * @returns {Observable} that must be subscribed to for the request
   *                       to be sent.
   */
  deleteApiKey(key: ApiKey): Observable<void> {
    return this.http.delete<void>(`/users/apikey/${key.id}`);
  }

  /**
   * Fetches the links from the backend with no filters applied.
   * NOTE: this requires the user to be logged in.
   * TODO: handle calls that don't have a logged in user, instead of erroring.
   *
   * @returns {Observable} of the list of returned links. Can be empty an
   *                       empty list.
   */
  getLinksWithNoFilters(): Observable<Link[]> {
    // Empty params object so no filters are applied server side.
    return this.http.post<Link[]>('/links', JSON.stringify({}));
  }

  /**
   * Fetches a list of the users tags from the backend.
   * NOTE: this requires the user to be logged in.
   * TODO: handle calls that don't have a logged in user, instead of erroring.
   *
   * @returns {Observable} of the list of returned tags. Can be empty an
   *                       empty list.
   */
  getTags(): Observable<string[]> {
    return this.http.get<string[]>('/tags').pipe(
      // If the route errors or there is no auth token present, just return
      // an empty array.
      catchError(() => of([]))
    );
  }

  /**
   * Fetches a map of the application preferences from the backend.
   *
   * @returns {Observable} of the map of returned prefs. Can be empty an
   *                       empty map.
   */
  getPreferences(): Observable<Map<string, Preference>> {
    return this.preferences$;
  }

  /**
   * Sets a preference in the backend database.
   * NOTE: Some prefs are application wide, and some are user specific.
   * See `//app/src/main/java/dev/thesummit/rook/models/Preference.java` for
   * the list of app specific prefs.
   *
   * @param {Preference} pref - the new pref to save.
   * @returns {Observable} Http obsedrvable of the new pref.
   */
  setPreference(pref: Preference): Observable<Preference> {
    return this.http.put<Preference>('/prefs', JSON.stringify(pref)).pipe(
      map(pref => {
        this.newPref$.next(); // Toggle a reload of preferences to pick up
        // the change everywhere.
        return pref;
      })
    );
  }

  /**
   * Requests a new user account from the backend.
   *
   * @param {User} user - the user to send to the backend to save.
   * @returns {Observable} Http observable of the newly created user
   */
  createUser(user: User): Observable<User> {
    return this.http.put<User>('/users', JSON.stringify(user));
  }

  /**
   * Adds the link to the user's list of bookmarks.
   * NOTE: if successful, the new link will be emitted via newLinks$ observable.
   *
   * @param {Link} link - the link to send to the backend to save.
   * @returns {Observable} Http observable of the newly created link
   */
  createLink(link: Link): Observable<Link> {
    return this.http.put<Link>('/links', JSON.stringify(link)).pipe(
      map(link => {
        // submit the newly created Link to the new links Observable so
        // subscribers can act accordingly.
        this.newLinks$.next(link);
        return link;
      }),
      catchError((err: HttpErrorResponse) => {
        this.toast.showError(err.error.title);
        return EMPTY;
      })
    );
  }

  /**
   * Adds the link to the user's list of bookmarks.
   * NOTE: if successful, the new link will be emitted via newLinks$ observable.
   *
   * @param {Link} link - the link to send to the backend to save.
   * @returns {Observable} Http observable of the newly created link
   */
  updateLink(link: Link): Observable<Link> {
    if (!link.id) {
      return EMPTY;
    }
    return this.http
      .patch<Link>(`/links/${link.id}`, JSON.stringify(link))
      .pipe(
        map(link => {
          // submit the updated Link to the new links Observable so
          // subscribers can act accordingly.
          this.newLinks$.next(link);
          return link;
        })
      );
  }

  /**
   * Wraps the private {newLinks$} subject in an observable for subscribers.
   *
   * @returns {Observable} Observable that emits newly created links.
   */
  getNewLinksAsObservable(): Observable<Link> {
    return this.newLinks$.asObservable();
  }

  /**
   * Sends a request to the server to remove the link from the database.
   *
   * @param {Link} link - the link to delete.
   * @returns {Observable} The request observable which must be subscribed to
   *                       in order for the request to be sent.
   */
  deleteLink(link: Link): Observable<void> {
    return this.http
      .delete<void>(`/links/${link.id}`)
      .pipe(map(() => this.newLinks$.next(null)));
  }
}
