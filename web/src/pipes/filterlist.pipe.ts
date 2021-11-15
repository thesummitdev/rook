import {Pipe, PipeTransform} from '@angular/core';

@Pipe({
  name: 'filterList',
})
export class FilterList implements PipeTransform {
  transform(value: string[], term: string): string[] {
    return value.filter((element) => element.includes(term));
  }
}
