import { DOCUMENT } from '@angular/common';
import { Inject, Injectable } from '@angular/core';
import { EventManager } from '@angular/platform-browser';
import { Observable } from 'rxjs';

type Options = {
  element: any;
  keys: string;
  description: string;
};

@Injectable({ providedIn: 'root' })
export class HotkeysService {
  private static readonly INPUT_OVERRIDE_KEYS = ['Escape'];

  hotkeys: Map<string, string> = new Map();
  defaults: Partial<Options> = { element: this.document };

  constructor(
    private eventManager: EventManager,
    @Inject(DOCUMENT) private document: Document
  ) {}

  /**
   *
   * @param options
   */
  addShortcut(options: Partial<Options>): Observable<KeyboardEvent> {
    const merged = { ...this.defaults, ...options };
    const event = `keydown.${merged.keys}`;
    this.hotkeys.set(merged.keys, merged.description);

    return new Observable(observer => {
      const handler = (event: KeyboardEvent) => {
        // Do not pass events when the target of the event is a text input.
        if (
          !(event.target instanceof HTMLInputElement) ||
          HotkeysService.INPUT_OVERRIDE_KEYS.includes(event.key)
        ) {
          event.preventDefault();
          observer.next(event);
        }
      };

      const dispose = this.eventManager.addEventListener(
        merged.element,
        event,
        handler
      );

      return () => {
        dispose();
        this.hotkeys.delete(merged.keys);
      };
    });
  }
}
