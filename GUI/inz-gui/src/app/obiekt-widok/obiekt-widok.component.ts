import { HttpClient } from '@angular/common/http';
import { Component, EventEmitter, HostListener, OnInit, Output } from '@angular/core';
import { ControlContainer, FormArray, FormControl, FormGroup, Validators } from '@angular/forms';
import { InfoService } from '../service/info.service';

@Component({
  selector: 'app-obiekt-widok',
  templateUrl: './obiekt-widok.component.html',
  styleUrls: ['./obiekt-widok.component.css']
})
export class ObiektWidokComponent implements OnInit {
  file: File | null = null;
  typ='SISO'
  optionsQ = [-1, 0 ,1]
  czyError = false;
  @Output() updateEvent = new EventEmitter<FormArray>()
  obiektForm = new FormGroup({
    obiekt: new FormArray([
      new FormGroup({
        gain: new FormControl(1.0),
        r1: new FormControl(1.0),
        q1: new FormControl(1.0),
        r2: new FormControl(1.1),
        q2: new FormControl(1.0),
        t1: new FormControl(1.0),
        t2: new FormControl(1.0),
        t3: new FormControl(1.0),
        tp: new FormControl(1.0),
        delay: new FormControl(0),
      }),
      new FormGroup({
        plik: new FormControl(this.file),
        liczbaWejsc: new FormControl(1),
        liczbaWyjsc: new FormControl(1)
        }),
    ])
  })
  
  @HostListener('change', ['$event.target.files']) emitFiles( event: FileList ) {
    const file = event && event.item(0);
    if(file)
    {
      this.file = file;
      this.obiektForm.get('obiekt')?.get([1])?.patchValue({ plik: this.file });
      console.log(this.obiektForm.controls.obiekt.controls[1])
    }
  }

  pierwszeRownanie = " $ \\large G(s) = Gain * \\frac{(R1*s + Q1)(R2*s + Q2)}{(T1*s+1)(T2*s+1)(T3+3)}*e^{Delay}$  ";
  drugieRownanie =  " $ \\large G(s) = K * \\frac{(s-z1)}{(s-b1)(s-b2)(s-b3)}$";
  zmienne = [{id: 1, nazwa: "Gain"},
            {id: 2, nazwa: "R1"},
            {id: 3, nazwa: "Q1"},
            {id: 4, nazwa:"R2"},
            {id: 5, nazwa: "Q2"},
            {id: 6, nazwa: "T1"},
            {id: 7, nazwa: "T2"},
            {id: 8, nazwa: "T3"},
            {id: 9, nazwa: "Delay"},
            {id: 10, nazwa: "Tp"}];
  constructor(private http: HttpClient, private infoService:InfoService) { }
  resetMIMO(): void{
    this.obiektForm.get('obiekt')?.get([1])?.patchValue({ plik: null, liczbaWejsc: 1, liczbaWyjsc: 1 }); 
    this.file=null;
    this.czyError = false;
  }
  infoWejscieWyjscia(): void{
    this.infoService.infoMIMOInOut(this.obiektForm.get('obiekt.1.plik')).subscribe({next: response =>{
      this.czyError = false;
      this.obiektForm.get('obiekt.1.liczbaWejsc')?.setValue(response.wejscia)
      this.obiektForm.get('obiekt.1.liczbaWyjsc')?.setValue(response.wyjscia)
    },
    error: error => {
      this.czyError = true;
      this.obiektForm.get('obiekt')?.get([1])?.patchValue({ plik: null, liczbaWejsc: 1, liczbaWyjsc: 1 }); 
      this.file=null
    }})
  }
  ngOnInit(): void {
    this.updateEvent.emit(this.obiektForm.controls.obiekt);
    this.obiektForm.valueChanges.subscribe( value =>{
      this.updateEvent.emit(this.obiektForm.controls.obiekt);
      console.log(value)
    })
  }
}
