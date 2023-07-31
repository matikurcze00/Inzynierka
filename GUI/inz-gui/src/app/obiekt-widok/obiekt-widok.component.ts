import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import {
  Component,
  EventEmitter,
  HostListener,
  Input,
  OnInit,
  Output,
  SimpleChanges,
} from '@angular/core';
import {
  ControlContainer,
  FormArray,
  FormControl,
  FormGroup,
  Validators,
} from '@angular/forms';
import { InfoService } from '../service/info.service';

@Component({
  selector: 'app-obiekt-widok',
  templateUrl: './obiekt-widok.component.html',
  styleUrls: ['./obiekt-widok.component.css'],
})
export class ObiektWidokComponent implements OnInit {
  file: File | null = null;
  typ = 'SISO';
  optionsQ = [-1, 0, 1];
  czyError = false;
  @Input() typRegulatora: any;
  @Output() updateEvent = new EventEmitter<FormArray>();
  errorMessage = '';
  obiektForm = new FormGroup({
    obiekt: new FormArray([
      new FormGroup({
        gain: new FormControl(1.0),
        r1: new FormControl(0.0),
        q1: new FormControl(1.0),
        r2: new FormControl(0.0),
        q2: new FormControl(0),
        t1: new FormControl(10.0),
        t2: new FormControl(2.0),
        t3: new FormControl(0.0),
        tp: new FormControl(1.0),
        delay: new FormControl(0),
      }),

      new FormGroup({
        plik: new FormControl(this.file),
        liczbaWejsc: new FormControl(1),
        liczbaWyjsc: new FormControl(1),
      }),
      new FormGroup({
        a1: new FormControl(-0.8),
        a2: new FormControl(0.0),
        a3: new FormControl(0.0),
        a4: new FormControl(0.0),
        a5: new FormControl(0.0),
        b1: new FormControl(1.0),
        b2: new FormControl(0.5),
        b3: new FormControl(0.2),
        b4: new FormControl(0.0),
        b5: new FormControl(0.0),
      }),
    ]),
  });

  @HostListener('change', ['$event.target.files']) emitFiles(event: FileList) {
    const file = event && event.item(0);
    if (file) {
      this.file = file;
      this.obiektForm.get('obiekt')?.get([1])?.patchValue({ plik: this.file });
      console.log(this.obiektForm.controls.obiekt.controls[1]);
    }
  }
  ngOnChanges(changes: SimpleChanges) {
    if (changes['typRegulatora']) {
      if (this.typRegulatora == 'gpc') {
        console.log('GPC');
      } else {
        console.log('cos innego');
      }
    }
  }
  pierwszeRownanie =
    ' $ \\large G(s) = Gain * \\frac{(R1*s + Q1)(R2*s + Q2)}{(T1*s+1)(T2*s+1)(T3+3)}*e^{Delay}$  ';
  drugieRownanie = ' $ \\large G(s) = K * \\frac{(s-z1)}{(s-b1)(s-b2)(s-b3)}$';
  zmienne = [
    { id: 1, nazwa: 'Gain' },
    { id: 2, nazwa: 'R1' },
    { id: 3, nazwa: 'Q1' },
    { id: 4, nazwa: 'R2' },
    { id: 5, nazwa: 'Q2' },
    { id: 6, nazwa: 'T1' },
    { id: 7, nazwa: 'T2' },
    { id: 8, nazwa: 'T3' },
    { id: 9, nazwa: 'Delay' },
    { id: 10, nazwa: 'Tp' },
  ];
  constructor(private http: HttpClient, private infoService: InfoService) {}
  resetMIMO(): void {
    this.obiektForm
      .get('obiekt')
      ?.get([1])
      ?.patchValue({ plik: null, liczbaWejsc: 1, liczbaWyjsc: 1 });
    this.file = null;
    this.czyError = false;
  }
  infoWejscieWyjscia(): void {
    if (this.typRegulatora == 'gpc') {
      this.infoService
        .infoMIMORownianiaInOut(this.obiektForm.get('obiekt.1.plik'))
        .subscribe({
          next: (response) => {
            this.czyError = false;
            this.obiektForm
              .get('obiekt.1.liczbaWejsc')
              ?.setValue(response.wejscia);
            this.obiektForm
              .get('obiekt.1.liczbaWyjsc')
              ?.setValue(response.wyjscia);
          },
          error: (error) => {
            this.czyError = true;
            this.obiektForm
              .get('obiekt')
              ?.get([1])
              ?.patchValue({ plik: null, liczbaWejsc: 1, liczbaWyjsc: 1 });
              const httpError = error as HttpErrorResponse
              if(httpError.status == 400) {
                this.errorMessage = 'Plik który został wysłany nie może zostać przekonwertowany na obiekt'
              } else {
                this.errorMessage = 'Błąd serwera'
              }
              console.log("aaaaaaa")
              console.log(this.errorMessage)
              this.file = null;
          },
        });
    } else {
      this.infoService
        .infoMIMODPAInOut(this.obiektForm.get('obiekt.1.plik'))
        .subscribe({
          next: (response) => {
            this.czyError = false;
            this.obiektForm
              .get('obiekt.1.liczbaWejsc')
              ?.setValue(response.wejscia);
            this.obiektForm
              .get('obiekt.1.liczbaWyjsc')
              ?.setValue(response.wyjscia);
          },
          error: (error) => {
            this.czyError = true;
            this.obiektForm
              .get('obiekt')
              ?.get([1])
              ?.patchValue({ plik: null, liczbaWejsc: 1, liczbaWyjsc: 1 });
              const httpError = error as HttpErrorResponse
              if(httpError.status == 400) {
                this.errorMessage = 'Plik który został wysłany nie może zostać przekonwertowany na obiekt'
              } else {
                this.errorMessage = 'Błąd serwera - sprawdź czy jest włączony'
              }
              console.log("aaaaaaa")
              console.log(this.errorMessage)
              this.file = null;
          },
        });
    }
  }
  ngOnInit(): void {
    this.updateEvent.emit(this.obiektForm.controls.obiekt);
    this.obiektForm.valueChanges.subscribe((value) => {
      this.updateEvent.emit(this.obiektForm.controls.obiekt);
      console.log(value);
    });
  }
}
