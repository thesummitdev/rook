import { A11yModule } from '@angular/cdk/a11y';
import { NgModule } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { BrowserModule } from '@angular/platform-browser';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { PipesModule } from 'web/src/pipes/pipes.module';

import { PillComponent } from './pill/pill.component';
import { SelectComponent } from './select/select.component';

@NgModule({
  declarations: [PillComponent, SelectComponent],
  imports: [
    A11yModule,
    BrowserModule,
    BrowserAnimationsModule,
    FormsModule,
    PipesModule,
  ],
  exports: [PillComponent, SelectComponent],
})
export class UiModule {}
