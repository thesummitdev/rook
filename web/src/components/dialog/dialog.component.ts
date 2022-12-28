import {Component, ElementRef, HostListener, Inject, ViewChild} from '@angular/core';
import {Observable, Subject} from 'rxjs';
import {DIALOG_CONTAINER} from 'web/src/util/injectiontokens';

import {DialogContainer} from './dialog.container.component';
import {DialogResult} from './dialog.result';

@Component({template: '<div></div>'})
/** Required methods and properties on Dialogs */
export abstract class DialogComponent<T> {
  constructor(
      @Inject(DIALOG_CONTAINER) protected readonly container: DialogContainer,
  ) {}

  /** Subject for setting the dialog result. */
  protected readonly result$: Subject<DialogResult<T>> = new Subject();

  /** Optional root element of the dialog. Mark as #dialog in the template */
  @ViewChild('dialog') protected rootEl!: ElementRef<HTMLDivElement>;

  /**
   * An Observable stream that will eventually emit the results of the dialog.
   * @return obs
   */
  resultAsObservable(): Observable<DialogResult<T>> {
    return this.result$.asObservable();
  }

  @HostListener('document:click', ['$event'])
  /**
   * Click listener that closes the dialog if the click location was outside of
   * the dialog.
   * NOTE: This is a NOOP if the dialog does not have a #dialog tag in the
   * template.
   * @param event
   */
  protected listenForOutsideClicks(event: PointerEvent): void {
    if (!this.rootEl?.nativeElement.contains(event.target as Node)) {
      this.cancel();
    }
  }

  /** Exit the dialog. */
  close(): void {
    this.container.exit();
  }

  /** Exit the dialog and emit a canclled result. */
  cancel(): void {
    this.setResult({cancelled: true});
    this.container.exit();
  }

  /**
   * Emit the results of the dialog and end the observable.
   * @param result
   */
  protected setResult(result: DialogResult<T>): void {
    this.result$.next(result);
    this.result$.complete();
  }
}
