import {HttpClient, HttpErrorResponse} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {Observable, of, ReplaySubject} from 'rxjs';
import {catchError, map} from 'rxjs/operators';
import {User} from 'web/src/models/user';

import {ToastService} from './toast.service';

@Injectable({providedIn: 'root'})
/** Login service that manages the user logged in state. */
export class LoginService {
  private token = new ReplaySubject<string|undefined>(1);
  private user = new ReplaySubject<User|undefined>(1);

  constructor(
      private readonly http: HttpClient,
      private readonly toast: ToastService,
  ) {
    this.token.next(undefined);
    this.user.next(undefined);
  }

  /**
   * Attempts to sign the user in with the given credentials.
   * @param username
   * @param password
   * @return Observable with the username
   */
  attemptSignIn(username: string, password: string):
      Observable<User|undefined> {
    return this.http.post('/login', {username, password})
        .pipe(
            map((resp: {jwt: string, username: string}) => {
              this.token.next(resp.jwt);
              const user: User = {username};
              this.user.next(user);
              this.toast.showMessage(`Welcome back, ${username}!`);
              return user;
            }),
            catchError(
                (err: HttpErrorResponse,
                 caught: Observable<User|undefined>) => {
                  if (err.status === 401) {
                    this.toast.showError('Invalid username and password.');
                  } else {
                    this.toast.showError(
                        'Unable to log you in, there was an unknown error.');
                  }
                  return of(undefined);
                }),
        );
  }

  /**
   * Signs the current user out.
   * Pushes empty values to token and user so that downstream components
   * can react accordingly.
   */
  signOut(): void {
    this.token.next('');  // This will cause CookieService to clear the cookie.
    this.user.next(undefined);
  }

  /**
   * Returns the current auth token as an observable.
   * @return Observable of the auth token.
   */
  getTokenAsObservable(): Observable<string|undefined> {
    return this.token.asObservable();
  }

  /**
   * Returns the current user as an observable.
   * @return Observable of the user.
   */
  getUserAsObservable(): Observable<User|undefined> {
    return this.user.asObservable();
  }

  /**
   * Sets the current auth token and broadcasts to all subscribers.
   * @param token
   */
  setToken(token: string|undefined): void {
    this.token.next(token);
  }

  /**
   * Sets the current user and broadcasts to all subscribers.
   * @param user
   */
  setUser(user: User): void {
    this.user.next(user);
  }
}
