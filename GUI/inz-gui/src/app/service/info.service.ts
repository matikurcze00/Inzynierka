import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { OdpowiedzInfoMIMO } from '../model/odpowiedzInfoMIMO';

@Injectable({
  providedIn: 'root'
})
export class InfoService {
  constructor(private http: HttpClient) { }
  private readonly api = 'http://localhost:8080/info'


  makeJSON(this: any, key:string, value:any){
    if(value ==="") {
      return null
    }
    return value;
  }

  public infoMIMODPAInOut(file: any): Observable<OdpowiedzInfoMIMO>{
    const formData = new FormData();
    formData.append('file', file.value);
    return this.http.post<OdpowiedzInfoMIMO>(
      this.api+'/MIMO/DPA',
      formData
    )
  }

  public infoMIMORownianiaInOut(file: any): Observable<OdpowiedzInfoMIMO>{
    const formData = new FormData();
    formData.append('file', file.value);
    return this.http.post<OdpowiedzInfoMIMO>(
      this.api+'/MIMO/Rownania',
      formData
    )
  }
}
