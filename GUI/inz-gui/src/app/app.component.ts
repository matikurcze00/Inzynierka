import { Component , ChangeDetectorRef, ViewChild, ElementRef } from '@angular/core';
import { FormArray, FormControl, FormGroup } from '@angular/forms';
import { Chart, registerables  } from 'chart.js';
import { Odpowiedz } from './model/odpowiedz';
import { OdpowiedzMIMO } from './model/odpowiedzMIMO';
import { WykresDane } from './model/wykresDane';
import { datasetsMIMO } from './model/datasetsMIMO';
import { StrojenieService } from './service/strojenie.service';
import { OdpowiedzSkokowaService } from './service/odpowiedz-skokowa.service';
import { OdpowiedzSkokowa } from './model/odpowiedzSkokowa';
import { HttpErrorResponse } from '@angular/common/http';
 
@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})

export class AppComponent {
  zakladka = "model"
  @ViewChild('fileInput') fileInput: ElementRef<HTMLInputElement>;
  
  constructor(private strojenieService: StrojenieService, private odpowiedzSkokowaService: OdpowiedzSkokowaService,
     private cdRef: ChangeDetectorRef) 
  {Chart.register(...registerables);
    this.fileInput = new ElementRef(document.createElement('input'));
  this.fileInput.nativeElement.type = 'file';
  this.fileInput.nativeElement.style.display = 'none';}
  odpowiedz?: Odpowiedz|null;
  OdpowiedzSkokowa?: OdpowiedzSkokowa|null;
  odpowiedzMIMO?: OdpowiedzMIMO|null;
  myChart?: any;
  odpSkokowaChart?: any;
  sterowanieChart?: any;
  liczbaRegulatorow?: any;
  file: File | null = null;
  fileWizualizacji: File | null = null;
  primaryXAxis = {valueType: 'krok'}
  title = 'Symulacje obiektu regulowanego'
  rownanie = " $ \\large G(s) = Gain * \\frac{(R1*s + Q1)(R2*s + Q2)}{(T1*s+1)(T2*s+1)(T3+3)}*e^{Delay}$  ";
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
  optionsQ = [-1, 0 ,1]

  chartData?: WykresDane;
  strojenie = new FormGroup({
    parObiekt: new FormGroup({
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
    MIMO: new FormGroup({
          plik: new FormControl(this.file),
          liczbaWejsc: new FormControl(1),
          liczbaWyjsc: new FormControl(1),
          plikWizualizacji: new FormControl(this.fileWizualizacji)
        }),
    parRegulator: new FormGroup({
      typ: new FormControl(),
      uMin: new FormControl(),
      uMax: new FormControl(),
      duMax: new FormControl(),
    }),
    parWizualizacja: new FormGroup({
      yZad: new FormControl([10.0]),
      yPP: new FormControl([0.0]),
      uPP: new FormControl([0.0]),
      skok: new FormControl([0.0]),
      dlugosc: new FormControl(100.0),
      strojenie: new FormControl([null,null,null] as (number | null)[]),
      blad: new FormControl('srednio')
    }),
    parObiektSymulacji : new FormGroup({
      gain: new FormControl(),
      r1: new FormControl(),
      q1: new FormControl(),
      r2: new FormControl(),
      q2: new FormControl(),
      t1: new FormControl(),
      t2: new FormControl(),
      t3: new FormControl(),
      tp: new FormControl(),
      delay: new FormControl(),
    }),
    })
    liczbaWyjscArray = [0]
    liczbaWejscArray = [0]
    liczbaWejsc : number|null = 1;
    typRegulatora = 'pid'
    yZadTemp = this.strojenie.get('parWizualizacja.yZad')?.value
    yPPTemp = this.strojenie.get('parWizualizacja.yPP')?.value
    uPPTemp = this.strojenie.get('parWizualizacja.uPP')?.value
    skokTemp = this.strojenie.get('parWizualizacja.skok')?.value
    dlugoscTemp = this.strojenie.get('parWizualizacja.dlugosc')?.value
    strojenieTemp = this.strojenie.get('parWizualizacja.strojenie')?.value
    zakladkaWizualizacja : String = "przebieg"
    czyLaduje = false;
    czyError=true;
    errorInfo = "Wystapil blad";
    czyInnyObiekt = false;
    updateObiekt(updatedObiekt: FormArray) {
      console.log("updateObiekt")
      console.log(updatedObiekt)
      if(updatedObiekt.controls['0']!=undefined){
          this.strojenie.controls.parObiekt.patchValue(updatedObiekt.controls['0'].value)
          this.strojenie.controls.MIMO.patchValue(updatedObiekt.controls['1'].value)
          this.file=updatedObiekt.controls['1'].value;
          if(this.file==null)
          {
            this.fileWizualizacji = null
          }
          if(this.strojenie.controls.MIMO.controls.liczbaWyjsc.value != this.liczbaWyjscArray.length ||
            this.strojenie.controls.MIMO.controls.liczbaWejsc.value!=this.liczbaWejsc
            )
          {
            if(this.strojenie.controls.MIMO.controls.liczbaWyjsc.value!=null)
              this.liczbaWejsc = this.strojenie.controls.MIMO.controls.liczbaWejsc.value;
            console.log("wchodzi")
            console.log(this.strojenie.controls.MIMO.controls.liczbaWyjsc.value)
            console.log(this.strojenie.controls.MIMO.controls.liczbaWejsc.value)

            this.strojenie.controls.parWizualizacja.controls.yZad.setValue(new Array(this.strojenie.controls.MIMO.controls.liczbaWyjsc.value).fill(0) as number[]); 
            this.strojenie.controls.parWizualizacja.controls.yPP.setValue(new Array(this.strojenie.controls.MIMO.controls.liczbaWyjsc.value).fill(0) as number[]); 
            this.strojenie.controls.parWizualizacja.controls.uPP.setValue(new Array(this.strojenie.controls.MIMO.controls.liczbaWejsc.value).fill(0) as number[]); 
            this.strojenie.controls.parWizualizacja.controls.skok.setValue(new Array(this.strojenie.controls.MIMO.controls.liczbaWyjsc.value).fill(0) as number[]); 
            
            this.yZadTemp = this.strojenie.get('parWizualizacja.yZad')?.value;
            this.yPPTemp = this.strojenie.get('parWizualizacja.yPP')?.value;
            this.uPPTemp = this.strojenie.get('parWizualizacja.uPP')?.value;
            this.skokTemp = this.strojenie.get('parWizualizacja.skok')?.value;

            if(this.strojenie.controls.MIMO.controls.liczbaWyjsc.value && this.strojenie.controls.MIMO.controls.liczbaWejsc.value )
            {
              console.log("wchodzi")
              if(this.strojenie.controls.parRegulator.controls.typ.value=='pid')
                this.strojenie.controls.parWizualizacja.controls.strojenie.setValue(new Array(this.strojenie.controls.MIMO.controls.liczbaWejsc.value*3).fill(null));
              else
                this.strojenie.controls.parWizualizacja.controls.strojenie.setValue(new Array(this.strojenie.controls.MIMO.controls.liczbaWejsc.value).fill(null));
              console.log(this.strojenie.controls.MIMO.controls.liczbaWejsc.value)
              this.strojenieTemp = this.strojenie.get('parWizualizacja.strojenie')?.value;
              this.liczbaWyjscArray = Array.from({length:this.strojenie.controls.MIMO.controls.liczbaWyjsc.value}, (_,i) => i);
              this.liczbaWejscArray = Array.from({length:this.strojenie.controls.MIMO.controls.liczbaWejsc.value}, (_,i) => i);
            }
          }      
        console.log(this.strojenie.controls.parWizualizacja)
      }
  }
  updateRegulator(updatedRegulator: FormArray) {
    if(updatedRegulator.controls['0']!=undefined){
      this.strojenie.controls.parRegulator.patchValue(updatedRegulator.controls['0'].value)

      if(this.strojenie.controls.MIMO.controls.liczbaWejsc.value)
      {
        if(this.strojenie.controls.parRegulator.controls.typ.value!=this.typRegulatora)
        {
          this.typRegulatora=this.strojenie.controls.parRegulator.controls.typ.value
          if(this.typRegulatora=='pid')
          {  
            this.strojenie.controls.parWizualizacja.controls.strojenie.setValue(new Array(this.strojenie.controls.MIMO.controls.liczbaWejsc.value*3).fill(null));
          }else{
            this.strojenie.controls.parWizualizacja.controls.strojenie.setValue(new Array(this.strojenie.controls.MIMO.controls.liczbaWejsc.value).fill(null));
          }
          this.strojenieTemp =this.strojenie.get('parWizualizacja.strojenie')?.value;
        }
      }
    }
  }
  setBlad(nazwa: string)
  {
    (<FormControl>this.strojenie.get('parWizualizacja.blad')).setValue(nazwa)
  }
  onYZadChange(index: number, value: any) {
    let yZad = this.strojenie.get('parWizualizacja.yZad')?.value;
    if(value && yZad)
    { 
      yZad[index] = value
      this.strojenie.get('parWizualizacja.yZad')!.setValue(yZad)
    }
  }
  onYPPChange(index: number, value: any) {
    let yPP = this.strojenie.get('parWizualizacja.yPP')?.value;
    if(value && yPP)
    { 
      yPP[index] = value
      this.strojenie.get('parWizualizacja.yPP')!.setValue(yPP)
    }
  }
  onUPPChange(index: number, value: any) {
    let uPP = this.strojenie.get('parWizualizacja.uPP')?.value;
    if(value && uPP)
    { 
      uPP[index] = value
      this.strojenie.get('parWizualizacja.uPP')!.setValue(uPP)
    }
  }
  onSkokChange(index: number, value: any) {
    let skok = this.strojenie.get('parWizualizacja.skok')?.value;
    if(value && skok)
    { 
      skok[index] = value
      this.strojenie.get('parWizualizacja.skok')!.setValue(skok)
    }
  }
  onStrojenieChange(index: number, value: any) {
    let strojenie = this.strojenie.get('parWizualizacja.strojenie')?.value;
    if(value && strojenie)
    { 
      strojenie[index] = value
      this.strojenie.get('parWizualizacja.strojenie')!.setValue(strojenie)
    }
    this.strojenieTemp=this.strojenie.get('parWizualizacja.strojenie')!.value
  }
  updateDlugosc(value: number) {
    this.strojenie.get('parWizualizacja.dlugosc')!.setValue(value);
  }

  onSubmit(){
    console.log("click")
    console.log(this.strojenie.controls.parWizualizacja)
    this.odpowiedzMIMO = null;
    this.odpowiedz = null;
    this.czyLaduje=true;
    this.czyError=false;
    if(this.strojenie.controls.MIMO.controls['plik'].value==null)
    {
    this.strojenieService.dobierzStrojenieSISO(this.strojenie).subscribe({next: response =>{
      this.czyLaduje=false;
      this.odpowiedz=response
      this.liczbaRegulatorow = [0];
      console.log(this.liczbaRegulatorow)
      if(this.odpowiedz!=null)
        this.odpowiedz.typRegulatora=this.strojenie.controls.parRegulator.value['typ']
      console.log("odpowiedz")
      console.log(this.odpowiedz)

      this.createChartData()
      this.createChart()
      this.createChartSterowania()
    },
    error: error => {
      this.czyError=true;
      this.errorInfo = error.message;
      console.log(error.message)
      console.error('There was an error!', error);
      console.log(error)
    }
    })}
  else{
    console.log("ok")
    this.strojenieService.dobierzStrojenieMIMO(this.strojenie).subscribe({next: response =>{
      this.czyLaduje=false;
      this.odpowiedzMIMO=response
      if(this.odpowiedzMIMO!=null)
        this.odpowiedzMIMO.typRegulatora=this.strojenie.controls.parRegulator.value['typ']
      console.log("odpowiedz")
      console.log(this.odpowiedzMIMO)
      this.liczbaRegulatorow = Array.from({length: this.odpowiedzMIMO.cel.length}, (_, i) => i);
      console.log(this.liczbaRegulatorow)
      this.createChartDataMIMO()
      this.createChartSterowaniaMIMO()
    },
    error: error => {
      this.czyError=true;
      this.errorInfo = error.message;
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
      for(var i = 0; i<this.odpowiedz.wykres.length; i++)
      {
        arrKroki[i] = i;
      }
      this.chartData = {
        kroki: arrKroki,
        cel: this.odpowiedz.cel,
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
      if(this.myChart)
        this.myChart.destroy()
      this.myChart = new Chart('myChart', {
      type: 'line',
      data : {
        labels: this.chartData?.kroki,
        datasets : [
          {
            label: "Wartość zadana wyjścia obiektu",
            data: this.chartData.cel,
            borderColor: 'blue',
            pointRadius: 3
          },
          {
            label: "Wyjście obiektu",
            data: this.chartData.obiekt,
            borderColor: 'Lime',
            pointRadius: 3
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
      for(var i = 0; i<this.odpowiedzMIMO.wykres[0].length; i++)
      {
        arrKroki[i] = i;
      }


      console.log("createChart")

      if(this.myChart)
        this.myChart.destroy()
      var chartDataSet : datasetsMIMO[] = new Array();
      for(let i = 1; i<this.odpowiedzMIMO.cel.length+1; i++)
      {
        chartDataSet.push({
          label: "Wartość zadana wyjścia " + i,
          data: this.odpowiedzMIMO.cel[i-1],
          borderColor: this.randomRGB(), 
          pointRadius: 3
        });
        chartDataSet.push({
          label: "Wartość wyjścia " + i,
          data: this.odpowiedzMIMO.wykres[i-1],
          borderColor: this.randomRGB(),
          pointRadius: 3 
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
    WyznaczOdpowiedz()
    {
      console.log("click")
      this.czyLaduje=true;
      this.czyError=false;
      if(this.strojenie.controls.MIMO.controls['plik'].value==null)
      {
        this.czyLaduje=false;
        this.odpowiedzSkokowaService.wyznaczOdpowiedzSISO(this.strojenie).subscribe({next: response =>{
          this.OdpowiedzSkokowa=response
          this.createChartOdpSkok()
        },
        error: error => {
          this.czyError=true;
          this.errorInfo = error.message;
          console.log(error.message)
          console.error('There was an error!', error);
          console.log(error)
        }})
      }
      else
      {
        this.odpowiedzSkokowaService.wyznaczOdpowiedzMIMO(this.strojenie).subscribe({next: response =>{
          this.czyLaduje=false;
          this.OdpowiedzSkokowa=response
          console.log(this.OdpowiedzSkokowa)
          this.createChartOdpSkokMIMO()
        },
        error: error => {
          this.czyError=true;
          this.errorInfo = error.message;
          console.log(error.message)
          console.error('There was an error!', error);
          console.log(error)
        }})
      }
    }
    createChartSterowania()
    {
      console.log("sterowanieSISOChart")
      if(this.odpowiedz)
      {
        var arrKroki: number[] = new Array(this.odpowiedz.sterowanie.length)
        for(var i = 0; i<this.odpowiedz.sterowanie.length; i++)
        {
          arrKroki[i] = i;
        }
      if(this.sterowanieChart)
        this.sterowanieChart.destroy()
      this.sterowanieChart = new Chart('sterowanieChart', {
      type: 'line',
      data : {
        labels: arrKroki,
        datasets : [
          {
            label: "Wejście obiektu",
            data: this.odpowiedz.sterowanie,
            backgroundColor: 'blue'
          }
        ]
      }
    });}
  }
  createChartSterowaniaMIMO()
    {
    console.log("createChartSterowaniaMIMO")
      if(this.odpowiedzMIMO)
      {
        var arrKroki: number[] = new Array(this.odpowiedzMIMO.sterowanie[0].length)
        for(var i = 0; i<this.odpowiedzMIMO.sterowanie[0].length; i++)
        {
          arrKroki[i] = i;
        }
      if(this.sterowanieChart)
        this.sterowanieChart.destroy()
      
      let liczbaWyjsc = this.strojenie.controls.MIMO.controls.liczbaWyjsc.value
      let liczbaWejsc = this.strojenie.controls.MIMO.controls.liczbaWejsc.value
      var chartDataSet : datasetsMIMO[] = new Array();
      if(liczbaWejsc!=null)   
        if(liczbaWyjsc!=null)
          for(let i = 1; i < liczbaWejsc+1; i++)
              {chartDataSet.push({
                label: "Wejscie " +i,
                data: this.odpowiedzMIMO.sterowanie[i-1],
                borderColor: this.randomRGB(),
                pointRadius: 3
              });
            }
      console.log(chartDataSet);
      let chartData = {
        labels: arrKroki,
        datasets: chartDataSet
      };
      
      this.sterowanieChart = new Chart('sterowanieChart', {
      type: 'line',
      data : chartData
    });
  }}
  createChartOdpSkok()
    {
      console.log("OdpSkokChart")
      if(this.OdpowiedzSkokowa)
      {
        var arrKroki: number[] = new Array(this.OdpowiedzSkokowa.przebieg[0].length)
        for(var i = 0; i<this.OdpowiedzSkokowa.przebieg[0].length; i++)
        {
          arrKroki[i] = i;
        }
      if(this.odpSkokowaChart)
        this.odpSkokowaChart.destroy()
      this.odpSkokowaChart = new Chart('odpSkokowaChart', {
      type: 'line',
      data : {
        labels: arrKroki,
        datasets : [
          {
            label: "Wyjście obiektu",
            data: this.OdpowiedzSkokowa.przebieg[0],
            borderColor: 'blue',
            pointRadius: 3
          }
        ]
      }
    });}
  }
  createChartOdpSkokMIMO()
    {
      console.log("OdpSkokMIMOChart")
      if(this.OdpowiedzSkokowa)
      {
        var arrKroki: number[] = new Array(this.OdpowiedzSkokowa.przebieg[0].length)
        for(var i = 0; i<this.OdpowiedzSkokowa.przebieg[0].length; i++)
        {
          arrKroki[i] = i;
        }
      if(this.odpSkokowaChart)
        this.odpSkokowaChart.destroy()
      
      let liczbaWyjsc = this.strojenie.controls.MIMO.controls.liczbaWyjsc.value
      let liczbaWejsc = this.strojenie.controls.MIMO.controls.liczbaWejsc.value
      var chartDataSet : datasetsMIMO[] = new Array();
      if(liczbaWejsc!=null)   
        if(liczbaWyjsc!=null)
          for(let i = 1; i < liczbaWejsc+1; i++)
            for(let j = 1; j<liczbaWyjsc+1; j++)
              {chartDataSet.push({
                label: "u-"+i+"y-" + j,
                data: this.OdpowiedzSkokowa.przebieg[(i-1)*liczbaWejsc+j-1],
                borderColor: this.randomRGB(),
                pointRadius: 3
              });
            }
      console.log(chartDataSet);
      let chartData = {
        labels: arrKroki,
        datasets: chartDataSet
      };
      
      this.odpSkokowaChart = new Chart('odpSkokowaChart', {
      type: 'line',
      data : chartData
    });
  }}
  onCheckboxChange(index: number, event: any) {
    let strojenie: (number | null)[] | null | undefined = this.strojenie.get('parWizualizacja.strojenie')?.value;
    if(strojenie) {
      if(strojenie[index])
      {
        strojenie[index] = null;
        
      }else{
        strojenie[index] = 1.0;
      }
      this.strojenie.get('parWizualizacja.strojenie')!.setValue(strojenie);
    }
  }
  isDisabled(index: number) {
    let strojenie = this.strojenie.get('parWizualizacja.strojenie')?.value;
    if(strojenie)
      return strojenie[index] == null;
    else
      return true;
  }
  onFileChange(event: any) {
    const file = event.target.files[0];
    this.fileWizualizacji = file;
    this.strojenie.get('MIMO.plikWizualizacji')?.patchValue(file);
    this.cdRef.detectChanges();
  }
  obiektSymulacjiInputs() {
    if(this.czyInnyObiekt)
    {
      this.strojenie.get('parObiektSymulacji')?.reset();
      this.strojenie.get('parObiektSymulacji')?.disable();
      this.fileWizualizacji=null;
      this.strojenie.get('MIMO.plikWizualizacji')!.reset();
      this.fileInput.nativeElement.value = '';
    }
    else
    {
      this.strojenie.get('parObiektSymulacji')?.enable();
      this.strojenie.get('parObiektSymulacji')?.patchValue(this.strojenie.get('parObiekt')!.value);
    }
    this.czyInnyObiekt = !this.czyInnyObiekt
    
  }
  ngOnInit(): void {
    this.strojenie.get('parObiektSymulacji')?.disable();
  }
  ngAfterViewInit() {
    this.cdRef.detectChanges();
  }
}