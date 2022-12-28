import { Directive } from '@angular/core';
import {
  AbstractControl,
  NG_VALIDATORS,
  ValidationErrors,
  Validator,
} from '@angular/forms';

@Directive({
  selector: '[strictRequired]',
  providers: [
    {
      provide: NG_VALIDATORS,
      useExisting: StrictRequiredDirective,
      multi: true,
    },
  ],
})
export class StrictRequiredDirective implements Validator {
  /**
   *
   * @param control
   */
  validate(control: AbstractControl): ValidationErrors | null {
    return control.value ? null : { strictRequired: { value: control.value } };
  }
}
