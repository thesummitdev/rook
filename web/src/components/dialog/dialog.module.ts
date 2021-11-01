import {OverlayModule} from '@angular/cdk/overlay';
import {PortalModule} from '@angular/cdk/portal';
import {NgModule} from '@angular/core';
import {ReactiveFormsModule} from '@angular/forms';
import {DialogContainerComponent} from './dialog.container.component';
import {LoginDialogComponent} from './login/login.dialog.component';

@NgModule({
  declarations: [DialogContainerComponent, LoginDialogComponent],
  imports: [
    OverlayModule,
    PortalModule,
    ReactiveFormsModule,
  ],
})
export class DialogModule {
}
