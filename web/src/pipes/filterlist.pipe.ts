import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'filterList',
})
export class FilterList implements PipeTransform {
  /**
   *
   * @param value
   * @param term
   */
  transform(value: string[] | null, term: string): string[] {
    if (!value) return [];
    return value.filter(element => element.includes(term));
  }
}
