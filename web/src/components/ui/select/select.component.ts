import {
  Component,
  ElementRef,
  EventEmitter,
  HostListener,
  Input,
  Output,
  ViewChild,
} from '@angular/core';

@Component({
  selector: 'app-select',
  templateUrl: './select.component.html',
  styleUrls: ['./select.component.scss'],
})
/**
   Generic implementation of a HTMLSelect element with a filterable
   autocomplete.
 */
export class SelectComponent {
  @Input() options: string[] = [];
  @Input() class: string = '';
  @Input() placeholder: string = '';
  @Output() selectionChanged: EventEmitter<string[]> = new EventEmitter();
  @ViewChild('root') root: ElementRef;
  @Input() selected: Set<string> = new Set();
  @ViewChild('input', { static: false }) input: ElementRef<HTMLInputElement>;

  showList = false;
  searchText: string = '';

  /**
   * Event handler when the text input recieves focus.
   *
   * @param _ the focus event.
   */
  onFocus(_: FocusEvent): void {
    this.showList = true;
  }

  /**
   *
   */
  focus(): void {
    this.input.nativeElement.focus();
  }

  /**
   * Event handler for the select autocomplete selection.
   *
   * @param option - the selected option.
   */
  onSelect(option: string): void {
    this.selected.add(option);
    this.selectionChanged.emit([...this.selected].sort());
    this.showList = false;
    this.searchText = '';
    this.focus();
  }

  /**
   * Global event listener on the root document that listens for clicks
   * outside of this SelectComponent. When a click outside is detected, it hides
   * the autocomplete list.
   *
   * @param event
   */
  @HostListener('document:click', ['$event'])
  maybeBlur(event: PointerEvent): void {
    if (
      event.target instanceof Node &&
      !(this.root.nativeElement as HTMLDivElement).contains(event.target)
    ) {
      this.showList = false;
    }
  }
}
