import { Directive } from '@angular/core';
import {
  AbstractControl,
  NG_VALIDATORS,
  ValidationErrors,
  Validator,
} from '@angular/forms';

@Directive({
  selector: '[urlRequired]',
  providers: [
    { provide: NG_VALIDATORS, useExisting: UrlRequiredDirective, multi: true },
  ],
})
export class UrlRequiredDirective implements Validator {
  /**
   *
   * @param control
   */
  validate(control: AbstractControl): ValidationErrors | null {
    let input = control.value;
    if (typeof input === 'string') {
      try {
        new URL(input);
      } catch (_) {
        return {
          urlRequired: { value: control.value, reason: 'malformed-url' },
        };
      }
    } else {
      return { urlRequired: { value: control.value, reason: 'invalid type' } };
    }

    return null;
  }
}
