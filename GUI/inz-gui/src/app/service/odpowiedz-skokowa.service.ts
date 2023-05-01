import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { OdpowiedzSkokowa } from '../model/odpowiedzSkokowa';

@Injectable({
  providedIn: 'root',
})
export class OdpowiedzSkokowaService {
  constructor(private http: HttpClient) {}
  private readonly api = 'http://localhost:8080/odpowiedz';
  makeJSON(this: any, key: string, value: any) {
    if (value === '') {
      return null;
    }
    return value;
  }
  public wyznaczOdpowiedzSISO(form: any): Observable<OdpowiedzSkokowa> {
    const headers = { 'Content-Type': 'application/json; charset=utf-8' };
    console.log('service');
    console.log(JSON.stringify(form.value, this.makeJSON));

    return this.http.post<OdpowiedzSkokowa>(
      this.api + '/SISO',
      JSON.stringify(form.value, this.makeJSON),
      { headers: headers }
    );
  }

  public wyznaczOdpowiedzMIMO(form: any): Observable<OdpowiedzSkokowa> {
    const formData = new FormData();
    formData.append('file', form.controls.MIMO.controls['plik'].value);
    formData.append('typ', form.controls.parRegulator.value.typ);
    formData.append('duMax', form.controls.parRegulator.value.duMax);
    formData.append('uMin', form.controls.parRegulator.value.uMin);
    formData.append('uMax', form.controls.parRegulator.value.uMax);
    formData.append('yZad', form.controls.parWizualizacja.value.yZad);
    formData.append('yPP', form.controls.parWizualizacja.value.yPP);
    formData.append('uPP', form.controls.parWizualizacja.value.uPP);
    formData.append('skok', form.controls.parWizualizacja.value.skok);
    formData.append('dlugosc', form.controls.parWizualizacja.value.dlugosc);

    return this.http.post<OdpowiedzSkokowa>(this.api + '/MIMO', formData);
  }
}
