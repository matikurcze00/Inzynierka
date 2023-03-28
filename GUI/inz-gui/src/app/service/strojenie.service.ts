import { Injectable } from '@angular/core';
import {HttpClient, HttpHeaders} from '@angular/common/http';
import { Observable } from 'rxjs';
import {Odpowiedz} from '../model/odpowiedz'
import { OdpowiedzMIMO } from '../model/odpowiedzMIMO';

@Injectable({
  providedIn: 'root'
})
export class StrojenieService {

  constructor(private http: HttpClient) { }
  private readonly api = 'http://localhost:8080/strojenie'

  makeJSON(this: any, key:string, value:any){
    if(value ==="") {
      return null
    }
    return value;
  }
  public dobierzStrojenieSISO(form: any): Observable<Odpowiedz> {
    const headers =  {'Content-Type' : 'application/json; charset=utf-8'};
    console.log("service")
    console.log(JSON.stringify(form.value,this.makeJSON))
    
    return this.http.post<Odpowiedz>(
      this.api+'/SISO',
      JSON.stringify(form.value,this.makeJSON),
      {headers: headers}
    );
  }
  public dobierzStrojenieMIMO(form: any): Observable<OdpowiedzMIMO> {
    const formData = new FormData();
    formData.append('file', form.controls.MIMO.controls['plik'].value);
    const plikWizualizacji = form.controls.MIMO.controls['plikWizualizacji'].value ? form.controls.MIMO.controls['plikWizualizacji'].value : form.controls.MIMO.controls['plik'].value;
    formData.append('file', plikWizualizacji);
    formData.append('file', form.controls.MIMO.controls['plikZaklocen'].value);
    formData.append('typ', form.controls.parRegulator.value.typ);
    formData.append('duMax', form.controls.parRegulator.value.duMax);
    formData.append('uMin', form.controls.parRegulator.value.uMin);
    formData.append('uMax', form.controls.parRegulator.value.uMax);
    formData.append('yZad', form.controls.parWizualizacja.value.yZad);
    formData.append('yPP', form.controls.parWizualizacja.value.yPP);
    formData.append('uPP', form.controls.parWizualizacja.value.uPP);
    formData.append('skok', form.controls.parWizualizacja.value.skok);
    formData.append('dlugosc', form.controls.parWizualizacja.value.dlugosc);
    formData.append('strojenie', form.controls.parWizualizacja.value.strojenie);
    formData.append('blad', form.controls.parWizualizacja.value.blad);
    
    return this.http.post<OdpowiedzMIMO>(
      this.api+'/MIMO',
      formData
    )
  }
}
