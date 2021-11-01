import {AfterViewInit, Component, ElementRef, HostListener, Inject, ViewChild} from '@angular/core';
import {AbstractControl, FormControl, FormGroup} from '@angular/forms';
import {Observable, Subject} from 'rxjs';
import {DIALOG_CONTAINER} from 'web/src/util/injectiontokens';
import {DialogComponent} from '../dialog.component';
import {DialogContainer} from '../dialog.container.component';
import {DialogResult} from '../dialog.result';

/** The data returned by the login dialog. */
interface LoginData {
  username: string;
  password: string;
}

/** The data shape of the login form. */
interface LoginForm {
  username: AbstractControl;
  password: AbstractControl;
}

@Component({
  templateUrl: 'login.dialog.component.html',
  styleUrls: ['login.dialog.component.scss'],
})
/**
 * The dialog that collects the user's username and password via a displayed
 * form.
 */
export class LoginDialogComponent implements DialogComponent, AfterViewInit {
  private readonly result$: Subject<DialogResult<LoginData>> = new Subject();

  @ViewChild('dialog') rootEl: ElementRef<HTMLDivElement>;
  @ViewChild('usernameInput') usernameInput: ElementRef<HTMLInputElement>;

  readonly controls: LoginForm&{[key: string]: AbstractControl} = {
    username: new FormControl(),
    password: new FormControl(),
  };
  readonly fg = new FormGroup(this.controls);


  constructor(
      @Inject(DIALOG_CONTAINER) private readonly container: DialogContainer,
  ) {}

  ngAfterViewInit(): void {
    this.usernameInput.nativeElement.focus();
  }

  /**
   * An Observable stream that will eventually emit the results of the dialog.
   * @return obs
   */
  resultAsObservable(): Observable<DialogResult<LoginData>> {
    return this.result$.asObservable();
  }

  /**
   * Emit the results of the dialog and end the observable.
   * @param result
   */
  private setResult(result: DialogResult<LoginData>): void {
    this.result$.next(result);
    this.result$.complete();
  }

  @HostListener('document:click', ['$event'])
  /**
   * Click listener that closes the dialog if the click location was outside of
   * the dialog.
   * @param event
   */
  listenForOutsideClicks(event: PointerEvent): void {
    if (!this.rootEl.nativeElement.contains(event.target as Node)) {
      this.cancel();
    }
  }

  signIn(): void {
    if (this.fg.valid) {
      const username = this.controls.username.value;
      const password = this.controls.password.value;
      this.setResult({cancelled: false, result: {username, password}});
      this.container.exit();
    }
  }

  close(): void {
    if (this.fg.valid) {
      const username = this.controls.username.value;
      const password = this.controls.password.value;
      this.setResult({cancelled: false, result: {username, password}});
      this.container.exit();
    }
  }

  /** Exit the dialog and emit a canclled result. */
  cancel(): void {
    this.setResult({cancelled: true});
    this.container.exit();
  }
}
