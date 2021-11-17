import {HttpEvent, HttpHandler, HttpInterceptor, HttpRequest} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {switchMap} from 'rxjs/operators';
import {LoginService} from '../services/login.service';


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
  protected static protectedRoutes: string[] = [
    '/links',
    '/tags',
  ];

  constructor(private readonly login: LoginService) {}

  /**
   * Logic to add Auth token to the request headers for matched routes. If route
   * is not protected, then continue the request as a noop.
   * @param req
   * @param next
   * @returns the http event as an obs.
   */
  intercept(req: HttpRequest<any>, next: HttpHandler):
      Observable<HttpEvent<any>> {
    if (AuthRequiredInterceptor.protectedRoutes.some(
            (route) => req.url.includes(route))) {
      return this.login.getTokenAsObservable().pipe(
          switchMap((token) => {
            const newReq = req.clone(
                {headers: req.headers.set('Authorization', `Bearer ${token}`)});
            return next.handle(newReq);
          }),
      );
    }
    return next.handle(req);
  }
}
