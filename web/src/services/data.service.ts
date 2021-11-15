import {HttpClient} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {switchMap, withLatestFrom} from 'rxjs/operators';
import {Link} from '../models/link';
import {FilterService} from './filters.service';

import {LoginService} from './login.service';

@Injectable({providedIn: 'root'})
/** Data service that fetches data from the flink api. */
export class DataService {
  constructor(
      private readonly http: HttpClient,
      private readonly login: LoginService,
      private readonly filters: FilterService,
  ) {}


  getLinks(): Observable<Link[]> {
    return this.login.getTokenAsObservable().pipe(
        switchMap((token) => this.http.request<Link[]>('POST', '/links', {
          headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json',
          },
          body: JSON.stringify({}),
        })),
    );
  }

  getFilteredLinks(): Observable<Link[]> {
    return this.login.getTokenAsObservable().pipe(
        withLatestFrom(this.filters.getTagsAsObservable()),
        switchMap(
            ([token, tags]) => this.http.request<Link[]>('POST', '/links', {
              headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json',
              },
              body: JSON.stringify({tags: [...tags].join(' ')}),
            })),

    );
  }

  getTags(): Observable<string[]> {
    return this.login.getTokenAsObservable().pipe(
        switchMap((token) => this.http.get<string[]>('/tags', {
          headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json',
          },
        })));
  }
}
