import {NgModule} from '@angular/core';
import {BrowserModule} from '@angular/platform-browser';

import {AppRoutingModule} from './app-routing.module';
import {WizualizacjaComponent} from './wizualizacja';
import {RegulatorWidokComponent} from './regulator-widok/regulator-widok.component';
import {ObiektWidokComponent} from './obiekt-widok/obiekt-widok.component';
import {NaglowekComponent} from './naglowek/naglowek.component';
import {NgbModule} from '@ng-bootstrap/ng-bootstrap';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import {HttpClientModule} from '@angular/common/http';

@NgModule({
  declarations: [
    WizualizacjaComponent,
    RegulatorWidokComponent,
    ObiektWidokComponent,
    NaglowekComponent,
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    NgbModule,
    BrowserAnimationsModule,
    FormsModule,
    ReactiveFormsModule,
    HttpClientModule,
  ],
  providers: [],
  bootstrap: [WizualizacjaComponent],
})
export class AppModule {
}
