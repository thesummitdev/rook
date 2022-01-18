import {AfterViewInit, Component, ElementRef, EventEmitter, Input, Output, ViewChild} from '@angular/core';
import {Link} from 'web/src/models/link';

import {linkFormAnimations} from './linkform.animations';

@Component({
  selector: 'link-form',
  templateUrl: 'linkform.component.html',
  styleUrls: ['linkform.component.scss'],
  animations: [linkFormAnimations.formErrorMessage],
})
export class LinkFormComponent implements AfterViewInit {
  @Input() data: Link|null = null;
  @Output() formSubmit = new EventEmitter<Link|null>();
  editing: boolean = false;
  @ViewChild('titleInput') titleInput: ElementRef<HTMLInputElement>;

  model: Link = {
    url: '',
    tags: '',
    title: '',
  };

  constructor() {}

  ngAfterViewInit(): void {
    if (this.data !== null) {
      this.model = {...this.data};
      this.editing = true;
    }
    this.titleInput.nativeElement.focus();
  }

  onSubmit(): void {
    this.formSubmit.emit(this.model);
  }

  onCancel(): void {
    this.formSubmit.emit(null);
  }
}
