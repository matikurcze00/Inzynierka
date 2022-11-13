import { ComponentFixture, TestBed } from '@angular/core/testing';

import { NaglowekComponent } from './naglowek.component';

describe('NaglowekComponent', () => {
  let component: NaglowekComponent;
  let fixture: ComponentFixture<NaglowekComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ NaglowekComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(NaglowekComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
