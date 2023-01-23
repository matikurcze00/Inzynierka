import { Component } from '@angular/core';
import { FormArray, FormControl, FormGroup } from '@angular/forms';
import { Chart, registerables  } from 'chart.js';
import { Odpowiedz } from './model/odpowiedz';
import { OdpowiedzMIMO } from './model/odpowiedzMIMO';
import { WykresDane } from './model/wykresDane';
import { datasetsMIMO } from './model/datasetsMIMO';
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
  odpowiedz?: Odpowiedz|null;
  odpowiedzMIMO?: OdpowiedzMIMO|null;
  myChart?: any;
  file: File | null = null;
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
    MIMO: new FormGroup({
          plik: new FormControl(this.file),
        }),
    parRegulator: new FormGroup({
      typ: new FormControl(),
      uMax: new FormControl(),
      duMax: new FormControl(),
      blad: new FormControl("srednio")
    })
    })

  updateObiekt(updatedObiekt: FormArray) {
    console.log("updateObiekt")
    if(updatedObiekt.controls['0']!=undefined){
        console.log(updatedObiekt.controls['0'])
        console.log("dalej")
        this.strojenie.controls.parObiekt.patchValue(updatedObiekt.controls['0'].value)
        console.log("update Obiekt")
        this.file=updatedObiekt.controls['1'].value;
        this.strojenie.controls.MIMO.patchValue(updatedObiekt.controls['1'].value)
        console.log(this.strojenie.controls)
      }
    
  }
  updateRegulator(updatedRegulator: FormArray) {
    if(updatedRegulator.controls['0']!=undefined){
        this.strojenie.controls.parRegulator.patchValue(updatedRegulator.controls['0'].value)
    }
  }
  setBlad(nazwa: string)
  {
    (<FormControl>this.strojenie.get('parRegulator.blad')).setValue(nazwa)
  }
  onSubmit(){
    console.log("click")
    this.odpowiedzMIMO = null;
    this.odpowiedz = null;
    if(this.strojenie.controls.MIMO.controls['plik'].value==null)
    {
    this.strojenieService.dobierzStrojenieSISO(this.strojenie).subscribe({next: response =>{
      this.odpowiedz=response
      if(this.odpowiedz!=null)
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
  else{
    console.log("ok")
    this.strojenieService.dobierzStrojenieMIMO(this.strojenie).subscribe({next: response =>{
      this.odpowiedzMIMO=response
      if(this.odpowiedzMIMO!=null)
        this.odpowiedzMIMO.typRegulatora=this.strojenie.controls.parRegulator.value['typ']
      console.log("odpowiedz")
      console.log(this.odpowiedzMIMO)

      this.createChartDataMIMO()
    },
    error: error => {
      console.log(error.message)
      console.error('There was an error!', error);
      console.log(error)
    }})
  }} 
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
            label: "Cel" + "1",
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
    }}
    createChartDataMIMO()
    {
      if(this.odpowiedzMIMO)
      {
      var arrKroki: number[] = new Array(this.odpowiedzMIMO.wykres[0].length)
      var arrCel: number[][] = new Array(this.odpowiedzMIMO.cel.length)
      for(var i = 0; i < this.odpowiedzMIMO.cel.length; i++)
      {
        arrCel[i] = new Array(this.odpowiedzMIMO.wykres.length)
      }
      const zmiana:number = this.odpowiedzMIMO.wykres[0].length/this.odpowiedzMIMO.cel.length;
      for(var i = 0; i<this.odpowiedzMIMO.wykres[0].length; i++)
      {
        arrKroki[i] = i;
      }
      console.log("arrCel")
      console.log(arrCel);
      console.log(zmiana)
      for(var j = 0; j<this.odpowiedzMIMO.cel.length; j++)
        {
          for(var i = 0; i<this.odpowiedzMIMO.wykres[0].length; i++)
            {
              if(i>=zmiana*j)
              {
                arrCel[j][i] = this.odpowiedzMIMO.cel[j];
              }
              else
              {
                arrCel[j][i] = 0.0;
              }
          }
        }
      console.log("createChart")

      if(this.myChart)
        this.myChart.destroy()
      var chartDataSet : datasetsMIMO[] = new Array();
      for(let i = 1; i<this.odpowiedzMIMO.cel.length+1; i++)
      {
        chartDataSet.push({
          label: "Cel wyjscia " + i,
          data: arrCel[i-1],
          backgroundColor: this.randomRGB() 
        });
        chartDataSet.push({
          label: "Wyjscie " + i,
          data: this.odpowiedzMIMO.wykres[i-1],
          backgroundColor: this.randomRGB() 
        });
      }
      console.log(chartDataSet)
      let chartData = {
        labels: arrKroki,
        datasets: chartDataSet
      };
      this.myChart = new Chart('myChart', {
      type: 'line',
      data : chartData
    });
    }}
    randomNum = () => Math.floor(Math.random() * (235 - 52 + 1) + 52);
    randomRGB = () => `rgb(${this.randomNum()}, ${this.randomNum()}, ${this.randomNum()})`;

  }