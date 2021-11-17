import {HttpClient} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {switchMap} from 'rxjs/operators';
import {Link} from '../models/link';
import {FilterService} from './filters.service';

@Injectable({providedIn: 'root'})
/** Data service that fetches data from the flink api. */
export class DataService {
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
}
