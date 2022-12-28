import { Injectable, OnDestroy } from '@angular/core';
import { Subject } from 'rxjs';
import { skip, takeUntil } from 'rxjs/operators';
import { User } from 'web/src/models/user';

import { LoginService } from './login.service';

@Injectable({ providedIn: 'root' })
/** Service for handling cookies. */
export class CookieService implements OnDestroy {
  private unsubscribe = new Subject<void>(); // Clean up subscriptions.

  constructor(private readonly login: LoginService) {
    // Check for a stored authentication token.
    const storedToken = this.getCookie('jwt');
    const storedUser = this.getCookie('user');
    const storedAdmin = this.getCookie('userIsAdmin');

    // Pass the stored token to the login service.
    this.login.setToken(storedToken);
    if (storedUser) {
      this.login.setUser({
        username: storedUser,
        isAdmin: storedAdmin === 'true',
      });
    }

    // On future token updates, set the cookie.
    this.login
      .getTokenAsObservable()
      .pipe(
        skip(1), // Ignore first value since this is a ReplaySubject.
        takeUntil(this.unsubscribe)
      )
      .subscribe((token: string | undefined) =>
        token ? this.setCookie('jwt', token, 7) : this.removeCookie('jwt')
      );

    this.login
      .getUserAsObservable()
      .pipe(
        skip(1), // Ignore first value since this is a ReplaySubject.
        takeUntil(this.unsubscribe)
      )
      .subscribe((user: User | undefined) => {
        if (user) {
          this.setCookie('user', user.username, 7);
          user.isAdmin ? this.setCookie('userIsAdmin', 'true', 7) : null;
        } else {
          this.removeCookie('user');
        }
      });
  }

  /**
   *
   */
  ngOnDestroy(): void {
    // Send unsubscribe because the service is being destroyed.
    this.unsubscribe.next();
    this.unsubscribe.complete();
  }

  /**
   * Fetch a cookie by key
   *
   * @param name the name of the cookie
   * @returns the cookie value or undefined
   */
  getCookie(name: string): string | undefined {
    const cookieArray: string[] = document.cookie.split('; ');
    const cookieName = `${name}=`;

    const cookie = cookieArray.find(cookie => cookie.includes(cookieName));
    if (cookie) {
      return cookie.substring(cookieName.length, cookie.length);
    }
    return undefined;
  }

  /**
   * Set a cookie in the current document.
   *
   * @param name the name of the cookie
   * @param value the value of the cookie
   * @param expireDays number of days until the cookie expires
   * @param path the cookie path
   */
  setCookie(
    name: string,
    value: string,
    expireDays: number,
    path: string = '/'
  ): void {
    const d: Date = new Date();
    d.setTime(d.getTime() + expireDays * 24 * 60 * 60 * 1000);
    const expires: string = `expires=${d.toUTCString()}`;
    const cpath: string = path ? `; path=${path}` : '';
    document.cookie = `${name}=${value}; ${expires}${cpath}; SameSite=Strict;`;
  }

  /**
   *
   * @param name
   */
  removeCookie(name: string): void {
    const expires = new Date();
    expires.setTime(expires.getTime() + 1000);
    document.cookie = `${name}=${''}; ${expires}${''}; SameSite=Strict;`;
  }
}
