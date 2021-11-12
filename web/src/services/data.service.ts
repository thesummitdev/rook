import {HttpClient} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {switchMap} from 'rxjs/operators';
import {Link} from '../models/link';

import {LoginService} from './login.service';

@Injectable({providedIn: 'root'})
/** Data service that fetches data from the flink api. */
export class DataService {
  constructor(
      private readonly http: HttpClient,
      private readonly login: LoginService,
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

  getTags(): Observable<String[]> {
    return this.login.getTokenAsObservable().pipe(
        switchMap((token) => this.http.get<String[]>('/tags', {
          headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json',
          },
        })));
  }
}
