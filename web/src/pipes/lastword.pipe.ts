import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'lastword',
})
export class LastWord implements PipeTransform {
  /**
   *
   * @param value
   */
  transform(value: string): string {
    return value.split(' ').pop();
  }
}
