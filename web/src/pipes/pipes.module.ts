import {NgModule} from '@angular/core';
import {FilterList} from './filterlist.pipe';
import {TimeSince} from './timesince.pipe';

@NgModule({
  declarations: [
    FilterList,
    TimeSince,
  ],
  exports: [
    FilterList,
    TimeSince,
  ],
})
export class PipesModule {
}
