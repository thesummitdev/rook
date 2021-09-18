import {Injectable} from '@angular/core';

@Injectable({providedIn : 'root'})
export class DataService {

  constructor() { console.log('hello from data service'); }

  test(): void {}
}
