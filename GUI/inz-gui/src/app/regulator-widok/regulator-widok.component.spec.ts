import {ComponentFixture, TestBed} from '@angular/core/testing';

import {RegulatorWidokComponent} from './regulator-widok.component';

describe('RegulatorWidokComponent', () => {
  let component: RegulatorWidokComponent;
  let fixture: ComponentFixture<RegulatorWidokComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [RegulatorWidokComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(RegulatorWidokComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
