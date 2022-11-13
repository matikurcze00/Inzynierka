import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ObiektWidokComponent } from './obiekt-widok.component';

describe('ObiektWidokComponent', () => {
  let component: ObiektWidokComponent;
  let fixture: ComponentFixture<ObiektWidokComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ObiektWidokComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ObiektWidokComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
