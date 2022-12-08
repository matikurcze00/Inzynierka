import { Injectable } from '@angular/core';
import {HttpClient, HttpHeaders} from '@angular/common/http';
import { Observable } from 'rxjs';
import {Odpowiedz} from '../model/odpowiedz'
@Injectable({
  providedIn: 'root'
})
export class StrojenieService {

  constructor(private http: HttpClient) { }
  private readonly api = 'http://localhost:8080'

  makeJSON(this: any, key:string, value:any){
    if(value ==="") {
      return null
    }
    return value;
  }
  public dobierzStrojenie(form: any): Observable<Odpowiedz> {
    const headers =  {'Content-Type' : 'application/json; charset=utf-8'};
    const Json = { parObiekt: {'k': 10,'b' : [10.0]}}
    console.log("service")
    console.log(JSON.stringify(form.value,this.makeJSON))
    
    return this.http.post<Odpowiedz>(
      this.api+'/strojenie',
      // Json,
      JSON.stringify(form.value,this.makeJSON),
      {headers: headers}
    );
  }
}
