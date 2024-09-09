import {TestBed} from '@angular/core/testing';

import {OdpowiedzSkokowaService} from './odpowiedz-skokowa.service';

describe('OdpowiedzSkokowaService', () => {
  let service: OdpowiedzSkokowaService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(OdpowiedzSkokowaService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
