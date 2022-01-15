import {Component, Inject, Optional} from '@angular/core';
import {Link} from 'web/src/models/link';
import {DIALOG_CONTAINER, LINK} from 'web/src/util/injectiontokens';

import {DialogComponent} from '../dialog.component';
import {DialogContainer} from '../dialog.container.component';


@Component({
  templateUrl: 'editlink.dialog.component.html',
  styleUrls: ['editlink.dialog.component.scss'],
})
export class EditLinkComponent extends DialogComponent<Link> {
  dialogTitle: string = 'new';

  constructor(
      @Inject(DIALOG_CONTAINER) container: DialogContainer,
      @Inject(LINK) @Optional() public readonly link: Link,
  ) {
    super(container);

    if (this.link) {
      this.dialogTitle = 'update';
    }
  }

  onFormSubmit(link: Link|null): void {
    if (link) {
      this.setResult({result: link, cancelled: false});
    } else {
      this.setResult({cancelled: true});
    }
    this.container.exit();
  }
}
