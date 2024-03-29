import { HTTP_INTERCEPTORS, HttpClientModule } from '@angular/common/http';
import { NgModule } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { BrowserModule } from '@angular/platform-browser';
import { CdkTableModule } from '@angular/cdk/table';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { DialogModule } from 'web/src/components/dialog/dialog.module';
import { HeaderComponent } from 'web/src/components/header/header.component';
import { ToastModule } from 'web/src/components/toast/toast.module';

import { FilterPanelComponent } from '../components/filterpanel/filterpanel.component';
import { LinkComponent } from '../components/link/link.component';
import { LinkListComponent } from '../components/linklist/linklist.component';
import { UiModule } from '../components/ui/ui.module';
import { DirectivesModule } from '../directives/directives.module';
import { AuthRequiredInterceptor } from '../interceptors/authrequired.interceptor';
import { PipesModule } from '../pipes/pipes.module';
import { MainViewComponent } from '../views/main/main.component';
import { SettingsViewComponent } from '../views/settings/settings.component';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';

@NgModule({
  declarations: [
    AppComponent,
    FilterPanelComponent,
    HeaderComponent,
    LinkComponent,
    LinkListComponent,
    MainViewComponent,
    SettingsViewComponent,
  ],
  imports: [
    AppRoutingModule,
    CdkTableModule,
    BrowserAnimationsModule,
    BrowserModule,
    DirectivesModule,
    FormsModule,
    DialogModule,
    HttpClientModule,
    PipesModule,
    ToastModule,
    UiModule,
  ],
  bootstrap: [AppComponent],
  providers: [
    {
      provide: HTTP_INTERCEPTORS,
      useClass: AuthRequiredInterceptor,
      multi: true,
    },
  ],
})
export class AppModule {}
