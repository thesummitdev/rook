import {
  HttpEvent,
  HttpHandler,
  HttpInterceptor,
  HttpRequest,
} from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { switchMap } from 'rxjs/operators';

import { LoginService } from '../services/login.service';

@Injectable()
/**
 * An HttpInterceptor that protects backend routes that require a signed in
 * user. If those routes (or children of the base routes) are matched, it will
 * fetch the current auth token and appended it as a Authorization header in the
 * outgoing request.
 * TODO: Correctly handle requests when the user is not signed in by redirecting
 * them to the login dialog, and waiting for the dialog to be closed and
 * grabbing the new auth token.
 *
 */
export class AuthRequiredInterceptor implements HttpInterceptor {
  // Routes that can only be accessed with an auth token.
  protected static protectedRoutes: string[] = [
    '/links',
    '/tags',
    '/users/apikey',
  ];

  // Routes that have enhanced functionality if auth is provided.
  protected static optionalAuthRoutes: string[] = ['/prefs'];

  constructor(private readonly login: LoginService) {}

  /**
   * Logic to add Auth token to the request headers for matched routes. If route
   * is not protected, then continue the request as a noop.
   *
   * @param req
   * @param next
   * @returns the http event as an obs.
   */
  intercept(
    req: HttpRequest<any>,
    next: HttpHandler
  ): Observable<HttpEvent<any>> {
    // Check if the Route requires an Auth token.
    if (
      AuthRequiredInterceptor.protectedRoutes.some(route =>
        req.url.includes(route)
      )
    ) {
      return this.login.getTokenAsObservable().pipe(
        switchMap(token => {
          if (!token) {
            throw new Error(
              // Token is required for this request.
              "No auth token present can't handle request for protected route."
            );
          }
          const newReq = req.clone({
            headers: req.headers.set('Authorization', `Bearer ${token}`),
          });
          return next.handle(newReq);
        })
      );
    } else if (
      AuthRequiredInterceptor.optionalAuthRoutes.some(route =>
        req.url.includes(route)
      )
    ) {
      // If the route accepts optional auth, attach the token if present.
      return this.login.getTokenAsObservable().pipe(
        switchMap(token => {
          if (token) {
            const newReq = req.clone({
              headers: req.headers.set('Authorization', `Bearer ${token}`),
            });
            return next.handle(newReq);
          }

          // No token present, so just fetch the default for the route.
          return next.handle(req);
        })
      );
    }

    // Not a protected route, NOOP.
    return next.handle(req);
  }
}
