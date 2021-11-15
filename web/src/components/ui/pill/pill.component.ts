import {coerceBooleanProperty} from '@angular/cdk/coercion';
import {Component, EventEmitter, HostListener, Input, Output} from '@angular/core';
import {pillAnimations} from './pill.animations';

@Component({
  selector: 'app-pill',
  templateUrl: './pill.component.html',
  styleUrls: ['./pill.component.scss'],
  animations: [
    pillAnimations.fadeInOut,
    pillAnimations.growWidth,
  ],
})
/** A basic UI pill element that optionally can be removed */
export class PillComponent {
  protected _removeable: boolean = false;
  hovered: boolean = false;

  @Output() removed: EventEmitter<string> = new EventEmitter<string>();
  @Input() content: string = '';

  /**
   * Getter for the private _removable property.
   * @returns whether the pill is removable.
   */
  @Input()
  get removeable(): boolean {
    return this._removeable;
  }

  /**
   * Parses HTML attributes to set removeable trait.
   * @param attr
   * */
  set removeable(attr: unknown) {
    this._removeable = coerceBooleanProperty(attr);
  }

  /** Event listener for mouseenter. */
  @HostListener('mouseenter', ['$event'])
  onMouseEnter(): void {
    this.hovered = true;
  }

  /** Event listener for mouseleave. */
  @HostListener('mouseleave', ['$event'])
  onMouseExit(): void {
    this.hovered = false;
  }

  /**
   * Click handler for the remove button for removable pills.
   * @param event - the click event that is swallowed by this component.
   */
  onRemove(event: MouseEvent): void {
    event.stopPropagation();
    this.removed.emit(this.content);
  }
}
