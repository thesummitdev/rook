import {Component} from '@angular/core';
import {take} from 'rxjs';
import {Observable} from 'rxjs';
import {Link} from 'web/src/models/link';
import {DataService} from 'web/src/services/data.service';
import {ToastService} from 'web/src/services/toast.service';
import {UiService} from 'web/src/services/ui.service';

import {createPanelAnimations} from './createpanel.animations';


@Component({
  selector: 'app-create-panel',
  templateUrl: './createpanel.component.html',
  styleUrls: ['./createpanel.component.scss'],
  animations: [
    createPanelAnimations.growHeight,
    createPanelAnimations.formErrorMessage,
  ],
})
export class CreatePanelComponent {
  readonly show: Observable<boolean>;

  model: Link = {
    url: '',
    tags: '',
    title: '',
  };

  constructor(
      private readonly data: DataService,
      private readonly toast: ToastService,
      private readonly ui: UiService,
  ) {
    this.show = this.ui.getCreatePanelAsObservable();
  }

  onSubmit(): void {
    this.data.createLink(this.model).pipe(take(1)).subscribe(() => {
      this.toast.showMessage('Got it, I\'ll remember this one!');
      this.ui.setCreatePanelVisible(false);
      this.model = {
        url: '',
        tags: '',
        title: '',
      };
    });
  }

  onClose(event: MouseEvent): void {
    event.stopPropagation();
    this.model = {
      url: '',
      tags: '',
      title: '',
    };
    this.ui.setCreatePanelVisible(false);
  }
}
