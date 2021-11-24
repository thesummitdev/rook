import {HttpClient} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {Observable, Subject} from 'rxjs';
import {map, switchMap} from 'rxjs/operators';
import {Link} from '../models/link';
import {FilterService} from './filters.service';

@Injectable({providedIn: 'root'})
/** Data service that fetches data from the flink api. */
export class DataService {
  // Emit any newly created links individually so that components don't have
  // to refresh the entire list when a new link is added.
  private readonly newLinks$: Subject<Link> = new Subject();

  constructor(
      private readonly http: HttpClient,
      private readonly filters: FilterService,
  ) {}

  /**
   * Fetches the links from the backend, and includes applied filters as request
   * parameters.
   * NOTE: this requires the user to be logged in.
   * TODO: handle calls that don't have a logged in user, instead of erroring.
   * @returns Http observable of the list of returned links. Can be empty an
   *     empty list.
   */
  getLinks(): Observable<Link[]> {
    return this.filters.getTagsAsObservable().pipe(
        switchMap((tags) => this.http.request<Link[]>('POST', '/links', {
          body: JSON.stringify({tags: [...tags].join(' ')}),
        })),
    );
  }

  /**
   * Fetches a list of the users tags from the backend.
   * NOTE: this requires the user to be logged in.
   * TODO: handle calls that don't have a logged in user, instead of erroring.
   * @returns Http observable of the list of returned tags. Can be empty an
   *     empty list.
   */
  getTags(): Observable<string[]> {
    return this.http.get<string[]>('/tags');
  }

  /**
   * Adds the link to the user's list of bookmarks.
   * NOTE: if successful, the new link will be emitted via newLinks$ observable.
   * @param link - the link to send to the backend to save.
   * @returns Http observable of the newly created link
   */
  createLink(link: Link): Observable<Link> {
    return this.http.put<Link>('/links', JSON.stringify(link))
        .pipe(map((link) => {
          // submit the newly created Link to the new links Observable so
          // subscribers can act accordingly.
          this.newLinks$.next(link);
          return link;
        }));
  }
}
