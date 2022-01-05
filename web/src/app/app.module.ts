import {HTTP_INTERCEPTORS, HttpClientModule} from '@angular/common/http';
import {NgModule} from '@angular/core';
import {FormsModule} from '@angular/forms';
import {BrowserModule} from '@angular/platform-browser';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import {DialogModule} from 'web/src/components/dialog/dialog.module';
import {HeaderComponent} from 'web/src/components/header/header.component';
import {ToastModule} from 'web/src/components/toast/toast.module';

import {CreatePanelComponent} from '../components/createpanel/createpanel.component';
import {FilterPanelComponent} from '../components/filterpanel/filterpanel.component';
import {LinkComponent} from '../components/link/link.component';
import {LinkListComponent} from '../components/linklist/linklist.component';
import {UiModule} from '../components/ui/ui.module';
import {StrictRequiredDirective} from '../directives/strictrequired.directive';
import {UrlRequiredDirective} from '../directives/urlrequired.directive';
import {AuthRequiredInterceptor} from '../interceptors/authrequired.interceptor';
import {PipesModule} from '../pipes/pipes.module';
import {MainComponent} from '../views/main/main.component';

import {AppRoutingModule} from './app-routing.module';
import {AppComponent} from './app.component';

@NgModule({
  declarations: [
    AppComponent,
    CreatePanelComponent,
    FilterPanelComponent,
    HeaderComponent,
    LinkComponent,
    LinkListComponent,
    MainComponent,
    StrictRequiredDirective,
    UrlRequiredDirective,
  ],
  imports: [
    AppRoutingModule,
    BrowserAnimationsModule,
    BrowserModule,
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
export class AppModule {
}
