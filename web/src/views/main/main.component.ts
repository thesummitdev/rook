import {Component, OnDestroy, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {Observable, take, zip} from 'rxjs';
import {User} from 'web/src/models/user';
import {DataService} from 'web/src/services/data.service';
import {DialogService} from 'web/src/services/dialog.service';
import {LoginService} from 'web/src/services/login.service';
import {UiService} from 'web/src/services/ui.service';

@Component({
  selector: 'app-main',
  templateUrl: './main.component.html',
  styleUrls: ['./main.component.scss'],
})
/** Main view component */
export class MainViewComponent implements OnInit, OnDestroy {
  user$: Observable<User|undefined>;

  constructor(
      private readonly login: LoginService,
      private readonly ui: UiService,
      private readonly route: ActivatedRoute,
      private readonly dialog: DialogService,
      private readonly data: DataService,
      private readonly router: Router,
  ) {
    this.user$ = this.login.getUserAsObservable();
  }
  ngOnInit(): void {
    this.ui.setFilterPanelVisible(true);

    if (this.route.pathFromRoot.toString().includes('create')) {
      zip([
        this.user$,
        this.route.queryParamMap,
      ])
          .pipe(take(1))
          .subscribe(([user, params]) => {
            const title = params.has('title') ? params.get('title') : '';
            const url = params.has('url') ? params.get('url') : '';
            const tags = params.has('tags') ? params.get('tags') : '';

            if (user) {
              // If user exists then we are logged in.
              this.router.navigate(['/'], {
                queryParams:
                    {title: undefined, url: undefined, tags: undefined},
                queryParamsHandling: 'merge',
                replaceUrl: true
              });

              const dialog = this.dialog.showAddLinkDialog({url, tags, title});
              dialog.resultAsObservable().pipe(take(1)).subscribe((dialog) => {
                // If the dialog includes a result, then add the link to the
                // backend
                if (dialog.result) {
                  this.data.createLink(dialog.result).subscribe();
                }
              });
            }
          });
    }
  }

  ngOnDestroy(): void {
    this.ui.setFilterPanelVisible(false);
  }
}
