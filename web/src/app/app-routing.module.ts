import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {MainViewComponent} from 'web/src/views/main/main.component';
import {SettingsViewComponent} from 'web/src/views/settings/settings.component';


const routes: Routes = [
  {path: '', component: MainViewComponent},
  {path: 'settings', component: SettingsViewComponent},
];

@NgModule(
    {
      imports: [RouterModule.forRoot(routes)],
      exports: [RouterModule],
    },
    )
export class AppRoutingModule {
}
