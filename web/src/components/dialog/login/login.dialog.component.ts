import {AfterViewInit, Component, ElementRef, Inject, ViewChild} from '@angular/core';
import {AbstractControl, FormControl, FormGroup, Validators} from '@angular/forms';
import {DIALOG_CONTAINER} from 'web/src/util/injectiontokens';

import {DialogComponent} from '../dialog.component';
import {DialogContainer} from '../dialog.container.component';

/** The data returned by the login dialog. */
interface LoginData {
  username: string;
  password: string;
}

/** The data shape of the login form. */
interface LoginForm {
  username: FormControl;
  password: FormControl;
}

@Component({
  templateUrl: 'login.dialog.component.html',
  styleUrls: ['login.dialog.component.scss'],
})
/**
 * The dialog that collects the user's username and password via a displayed
 * form.
 */
export class LoginDialogComponent extends DialogComponent<LoginData> implements
    AfterViewInit {
  @ViewChild('usernameInput') usernameInput: ElementRef<HTMLInputElement>;

  readonly controls: LoginForm&{[key: string]: AbstractControl} = {
    username: new FormControl('', Validators.required),
    password: new FormControl('', Validators.required),
  };
  readonly fg = new FormGroup(this.controls);


  constructor(
      @Inject(DIALOG_CONTAINER) container: DialogContainer,
  ) {
    super(container);
  }

  ngAfterViewInit(): void {
    this.usernameInput.nativeElement.focus();
  }

  signIn(): void {
    if (this.fg.valid) {
      const username = this.controls.username.value;
      const password = this.controls.password.value;
      this.setResult({cancelled: false, result: {username, password}});
      this.container.exit();
    }
  }

  override close(): void {
    if (this.fg.valid) {
      const username = this.controls.username.value;
      const password = this.controls.password.value;
      this.setResult({cancelled: false, result: {username, password}});
      this.container.exit();
    }
  }

  /** Exit the dialog and emit a canclled result. */
  override cancel(): void {
    this.setResult({cancelled: true});
    this.container.exit();
  }
}
