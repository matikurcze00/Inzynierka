import { Component, EventEmitter, OnInit, Output } from '@angular/core';
import { FormArray, FormControl, FormGroup } from '@angular/forms';

@Component({
  selector: 'app-regulator-widok',
  templateUrl: './regulator-widok.component.html',
  styleUrls: ['./regulator-widok.component.css']
})
export class RegulatorWidokComponent implements OnInit {
  @Output() updateEvent = new EventEmitter<FormArray>()
  typ = [{id:1, typ:"PID"},
          {id:2, typ:"DMC"},
          {id:3, typ:"MPCs"}];
  regulatorForm = new FormGroup({
    regulator: new FormArray([
      new FormGroup({
        typ: new FormControl("pid"),
        uMin: new FormControl(0.0),
        uMax: new FormControl(100.0),
        duMax: new FormControl(3.0),
      })
    ])
  })
  constructor() { }

  ngOnInit(): void {
    this.updateEvent.emit(this.regulatorForm.controls.regulator);
  
    this.regulatorForm.valueChanges.subscribe( value =>{
      this.updateEvent.emit(this.regulatorForm.controls.regulator);
      console.log(value)
    
    })
  }

}
