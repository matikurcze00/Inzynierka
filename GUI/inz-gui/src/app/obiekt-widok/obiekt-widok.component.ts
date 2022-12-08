import { outputAst } from '@angular/compiler';
import { Component, EventEmitter, OnInit, Output } from '@angular/core';
import { FormArray, FormControl, FormGroup } from '@angular/forms';

@Component({
  selector: 'app-obiekt-widok',
  templateUrl: './obiekt-widok.component.html',
  styleUrls: ['./obiekt-widok.component.css']
})
export class ObiektWidokComponent implements OnInit {
  @Output() updateEvent = new EventEmitter<FormArray>()
  obiektForm = new FormGroup({
    obiekt: new FormArray([
      new FormGroup({
        k: new FormControl(1.0),
        z1: new FormControl(1.0),
        z2: new FormControl(1.0),
        b1: new FormControl(1.1),
        b2: new FormControl(1.0),
        b3: new FormControl(1.0),
        ts: new FormControl(1.0),
        opoznienie: new FormControl(0),
        szum: new FormControl(0.0),
      })
    ])
  })
  pierwszeRownanie = " G(s) = $ \\large K * \\frac{(s-z1)(s-z2)}{(s-b1)(s-b2)(s-b3)}$";
  zmienne = [{id: 1, nazwa: "K"},
            {id: 2, nazwa: "z1"},
            {id: 3, nazwa: "z2"},
            {id: 4, nazwa:"b1"},
            {id: 5, nazwa: "b2"},
            {id: 6, nazwa: "b3"},
            {id: 7, nazwa: "Ts"},
            {id: 8, nazwa: "Opoznienie"},
            {id: 9, nazwa: "Szum"}];
  constructor() { }

  ngOnInit(): void {
    this.updateEvent.emit(this.obiektForm.controls.obiekt);
    this.obiektForm.valueChanges.subscribe( value =>{
      this.updateEvent.emit(this.obiektForm.controls.obiekt);
      console.log(value)
    })
  }

}
