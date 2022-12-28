import {
  AfterViewInit,
  Component,
  Inject,
  OnDestroy,
  Optional,
} from '@angular/core';
import { ReplaySubject, takeUntil } from 'rxjs';
import { Link } from 'web/src/models/link';
import { HotkeysService } from 'web/src/services/hotkeys.service';
import {
  DIALOG_CONTAINER,
  EDIT_MODE,
  LINK,
} from 'web/src/util/injectiontokens';

import { DialogComponent } from '../dialog.component';
import { DialogContainer } from '../dialog.container.component';

@Component({
  templateUrl: 'editlink.dialog.component.html',
  styleUrls: ['editlink.dialog.component.scss'],
})
export class EditLinkComponent
  extends DialogComponent<Link>
  implements AfterViewInit, OnDestroy
{
  dialogTitle: string = 'new';
  private readonly destroyed$: ReplaySubject<void> = new ReplaySubject();

  constructor(
    private readonly hotkeys: HotkeysService,
    @Inject(DIALOG_CONTAINER) container: DialogContainer,
    @Inject(LINK) @Optional() public readonly link: Link,
    @Inject(EDIT_MODE) @Optional() public readonly editMode: boolean
  ) {
    super(container);

    if (this.editMode) {
      this.dialogTitle = 'update';
    }
  }

  /**
   *
   */
  ngAfterViewInit(): void {
    this.hotkeys
      .addShortcut({
        keys: 'Escape',
        element: this.rootEl.nativeElement,
        description: 'Closes the open dialog.',
      })
      .pipe(takeUntil(this.destroyed$))
      .subscribe(event => {
        event.stopPropagation();
        this.cancel();
      });
  }

  /**
   *
   */
  ngOnDestroy(): void {
    this.destroyed$.next();
  }

  /**
   *
   * @param link
   */
  onFormSubmit(link: Link | null): void {
    if (link) {
      this.setResult({ result: link, cancelled: false });
    } else {
      this.setResult({ cancelled: true });
    }
    this.container.exit();
  }
}
