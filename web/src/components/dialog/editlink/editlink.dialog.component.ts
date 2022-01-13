import {Component, Inject} from '@angular/core';
import {Link} from 'web/src/models/link';
import {DIALOG_CONTAINER, LINK} from 'web/src/util/injectiontokens';

import {DialogComponent} from '../dialog.component';
import {DialogContainer} from '../dialog.container.component';


@Component({
  templateUrl: 'editlink.dialog.component.html',
  styleUrls: ['editlink.dialog.component.scss'],
})
export class EditLinkComponent extends DialogComponent<Link> {
  constructor(
      @Inject(DIALOG_CONTAINER) container: DialogContainer,
      @Inject(LINK) public readonly link: Link,
  ) {
    super(container);
  }
}
