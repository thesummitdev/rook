import {HttpClientModule} from '@angular/common/http';
import {NgModule} from '@angular/core';
import {BrowserModule} from '@angular/platform-browser';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import {DialogModule} from 'web/src/components/dialog/dialog.module';
import {HeaderComponent} from 'web/src/components/header/header.component';
import {ToastModule} from 'web/src/components/toast/toast.module';
import {CreatePanelComponent} from '../components/createpanel/createpanel.component';
import {FilterPanelComponent} from '../components/filterpanel/filterpanel.component';
import {LinkComponent} from '../components/link/link.component';
import {LinkListComponent} from '../components/linklist/linklist.component';

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
  ],
  imports: [
    AppRoutingModule,
    BrowserAnimationsModule,
    BrowserModule,
    DialogModule,
    HttpClientModule,
    ToastModule,
  ],
  bootstrap: [AppComponent],
})
export class AppModule {
}
