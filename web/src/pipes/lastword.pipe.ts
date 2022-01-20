import {Pipe, PipeTransform} from '@angular/core';

@Pipe({
  name: 'lastword',
})
export class LastWord implements PipeTransform {
  transform(value: string): string {
    return value.split(' ').pop();
  }
}
