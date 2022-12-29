import { NgModule } from '@angular/core';

import { StrictRequiredDirective } from './strictrequired.directive';
import { UrlRequiredDirective } from './urlrequired.directive';

@NgModule({
  declarations: [StrictRequiredDirective, UrlRequiredDirective],
  exports: [StrictRequiredDirective, UrlRequiredDirective],
})
export class DirectivesModule {}
