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
      // Json,
      JSON.stringify(form.value,this.makeJSON),
      {headers: headers}
    );
  }
  public dobierzStrojenieMIMO(form: any): Observable<OdpowiedzMIMO> {
    const formData = new FormData();
    formData.append('file', form.controls.MIMO.controls['plik'].value);
    // formData.append('parRegulator', JSON.stringify(form.controls.parRegulator.value, this.makeJSON));    
    formData.append('typ', form.controls.parRegulator.value.typ);
    formData.append('duMax', form.controls.parRegulator.value.duMax);
    formData.append('uMax', form.controls.parRegulator.value.uMax);
    console.log("aaa")
    console.log(JSON.stringify(form.controls.parRegulator.value))
    console.log(formData.get('file'));
    console.log(formData)
    console.log(formData.get('parRegulator'))
    console.log('bbb')
    const headers2 =  new HttpHeaders({'Content-Type' : 'application/json; charset=utf-8'});

    const headers = new HttpHeaders({ 'Content-Type': 'multipart/form-data' });    
    return this.http.post<OdpowiedzMIMO>(
      this.api+'/MIMO',
      formData
    )
      
  }
}
