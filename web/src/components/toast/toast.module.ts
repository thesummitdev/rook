import { OverlayModule } from '@angular/cdk/overlay';
import { PortalModule } from '@angular/cdk/portal';
import { NgModule } from '@angular/core';

import { ToastComponent } from './toast.component';
import { ToastContainerComponent } from './toast.container.component';

@NgModule({
  declarations: [ToastContainerComponent, ToastComponent],
  imports: [OverlayModule, PortalModule],
})
export class ToastModule {}
