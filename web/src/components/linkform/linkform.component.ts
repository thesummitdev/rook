import {
  AfterViewInit,
  Component,
  ElementRef,
  EventEmitter,
  Inject,
  Input,
  Optional,
  Output,
  ViewChild,
} from '@angular/core';
import { shareReplay } from 'rxjs';
import { Link } from 'web/src/models/link';
import { DataService } from 'web/src/services/data.service';
import { EDIT_MODE } from 'web/src/util/injectiontokens';

import { linkFormAnimations } from './linkform.animations';

@Component({
  selector: 'link-form',
  templateUrl: 'linkform.component.html',
  styleUrls: ['linkform.component.scss'],
  animations: [linkFormAnimations.formErrorMessage],
})
export class LinkFormComponent implements AfterViewInit {
  @Input() data: Link | null = null;
  @Output() formSubmit = new EventEmitter<Link | null>();

  editing: boolean = false;
  tagOptions$ = this.dataService.getTags().pipe(shareReplay(1));

  @ViewChild('titleInput') titleInput: ElementRef<HTMLInputElement>;
  @ViewChild('tagsInput') tagInput: ElementRef<HTMLInputElement>;

  model: Link = {
    url: '',
    tags: '',
    title: '',
  };

  constructor(
    private readonly dataService: DataService,
    @Inject(EDIT_MODE) @Optional() public readonly editMode: boolean
  ) {}

  /**
   *
   */
  ngAfterViewInit(): void {
    if (this.data !== null) {
      this.model = { ...this.data };
    }
    if (this.editMode) {
      this.editing = true;
    }
    this.titleInput.nativeElement.focus();
  }

  /**
   *
   * @param event
   * @param tag
   */
  appendTag(event: Event, tag: string): void {
    event.stopPropagation();
    event.preventDefault();
    const tags = this.model.tags.split(' ');
    const lastPosition = tags.length - 1;
    tags.splice(lastPosition, 1, tag);
    this.model.tags = tags.join(' ');
    this.model.tags += ' ';
    this.tagInput.nativeElement.focus();
  }

  /**
   *
   */
  onSubmit(): void {
    // Convert all tags to lower case before emitting.
    this.model.tags = this.model.tags?.toLowerCase() ?? '';
    this.formSubmit.emit(this.model);
  }

  /**
   *
   * @param event
   */
  onCancel(event: Event): void {
    this.formSubmit.emit(null);
  }
}
