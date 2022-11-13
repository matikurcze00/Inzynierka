import { Component, OnInit } from '@angular/core';

@Component({
  selector: 'app-obiekt-widok',
  templateUrl: './obiekt-widok.component.html',
  styleUrls: ['./obiekt-widok.component.css']
})
export class ObiektWidokComponent implements OnInit {

  pierwszeRownanie = " G(z) = $ \\large\\frac{(z-a)(z-b)}{(z-c)(z-d)}$";
  zmienne = [{id: 1, nazwa: "a"},
            {id: 2, nazwa: "b"},
            {id: 3, nazwa:"c"},
            {id: 4, nazwa: "d"}];
  constructor() { }

  ngOnInit(): void {
  }

}
