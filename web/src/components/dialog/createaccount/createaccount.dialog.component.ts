import {
  AfterViewInit,
  Component,
  ElementRef,
  Inject,
  ViewChild,
} from '@angular/core';
import { DIALOG_CONTAINER } from 'web/src/util/injectiontokens';

import { DialogComponent } from '../dialog.component';
import { DialogContainer } from '../dialog.container.component';

/** The data returned by the login dialog. */
interface UserData {
  username: string;
  password: string;
}

@Component({
  templateUrl: 'createaccount.dialog.component.html',
  styleUrls: ['createaccount.dialog.component.scss'],
})
/**
 * The dialog that collects the user's username and password via a displayed
 * form.
 */
export class CreateAccountDialogComponent
  extends DialogComponent<UserData>
  implements AfterViewInit
{
  @ViewChild('usernameInput') usernameInput: ElementRef<HTMLInputElement>;

  model: UserData = { username: '', password: '' };

  constructor(@Inject(DIALOG_CONTAINER) container: DialogContainer) {
    super(container);
  }

  /**
   *
   */
  ngAfterViewInit(): void {
    this.usernameInput.nativeElement.focus();
  }

  /**
   *
   */
  submit(): void {
    const username = this.model.username;
    const password = this.model.password;
    this.setResult({ cancelled: false, result: { username, password } });
    this.container.exit();
  }

  /**
   *
   */
  override close(): void {
    this.container.exit();
  }

  /** Exit the dialog and emit a canclled result. */
  override cancel(): void {
    this.setResult({ cancelled: true });
    this.container.exit();
  }
}
