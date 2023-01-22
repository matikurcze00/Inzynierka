import { Component } from '@angular/core';
import { FormArray, FormControl, FormGroup } from '@angular/forms';
import { Chart, registerables  } from 'chart.js';
import { Odpowiedz } from './model/odpowiedz';
import { WykresDane } from './model/wykresDane';
import { StrojenieService } from './service/strojenie.service';
 
@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})

export class AppComponent {
  zakladka = "model"
  constructor(private strojenieService: StrojenieService) 
  {Chart.register(...registerables)}
  odpowiedz?: Odpowiedz;
  myChart?: any;
  primaryXAxis = {valueType: 'krok'}
  title = 'Symulacje obiektu regulowanego'
  chartData?: WykresDane;
  strojenie = new FormGroup({
    parObiekt: new FormGroup({
        k: new FormControl(),
        z1: new FormControl(),
        z2: new FormControl(),
        b1: new FormControl(),
        b2: new FormControl(),
        b3: new FormControl(),
        ts: new FormControl(),
        opoznienie: new FormControl(),
        szum: new FormControl(),
      }),
    parRegulator: new FormGroup({
      typ: new FormControl(),
      uMax: new FormControl(),
      duMax: new FormControl(),
    })
    })

  updateObiekt(updatedObiekt: FormArray) {
    if(updatedObiekt.controls['0']!=undefined){
        this.strojenie.controls.parObiekt.patchValue(updatedObiekt.controls['0'].value)
        console.log("update Obiekt")
      }
    
  }
  updateRegulator(updatedRegulator: FormArray) {
    if(updatedRegulator.controls['0']!=undefined){
        this.strojenie.controls.parRegulator.patchValue(updatedRegulator.controls['0'].value)
    }
  }
  onSubmit(){
    console.log("click")
    this.strojenieService.dobierzStrojenie(this.strojenie).subscribe({next: response =>{
      this.odpowiedz=response
      this.odpowiedz.typRegulatora=this.strojenie.controls.parRegulator.value['typ']
      console.log("odpowiedz")
      console.log(this.odpowiedz)

      this.createChartData()
      this.createChart()
    },
    error: error => {
      console.log(error.message)
      console.error('There was an error!', error);
      console.log(error)
    }
    })} 
    createChartData()
    {
      if(this.odpowiedz)
      {
      var arrKroki: number[] = new Array(this.odpowiedz.wykres.length)
      var arrCel: number[] = new Array(this.odpowiedz.wykres.length)
      for(var i = 0; i<this.odpowiedz.wykres.length; i++)
      {
        arrKroki[i] = i;
        arrCel[i] = this.odpowiedz.cel;
      }
      this.chartData = {
        kroki: arrKroki,
        cel: arrCel,
        obiekt: this.odpowiedz.wykres
      }
      console.log(this.chartData)
    }}
    createChart()
    {
      console.log("createChart")
      console.log(this.chartData)
      if(this.chartData)
      {
        // var context = document.getElementById('myChart').getContext("2d");
      if(this.myChart)
        this.myChart.destroy()
      this.myChart = new Chart('myChart', {
      type: 'line',
      data : {
        labels: this.chartData?.kroki,
        datasets : [
          {
            label: "Cel",
            data: this.chartData.cel,
            backgroundColor: 'blue'
          },
          {
            label: "Strojony obiekt",
            data: this.chartData.obiekt,
            backgroundColor: 'Lime'
          }
        ]
      }
    });
    }
    }
  }