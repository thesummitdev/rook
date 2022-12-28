import { NgModule } from '@angular/core';

import { FilterList } from './filterlist.pipe';
import { LastWord } from './lastword.pipe';
import { TimeSince } from './timesince.pipe';

@NgModule({
  declarations: [FilterList, TimeSince, LastWord],
  exports: [FilterList, TimeSince, LastWord],
})
export class PipesModule {}
