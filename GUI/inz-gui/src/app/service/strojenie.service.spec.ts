import {TestBed} from '@angular/core/testing';

import {StrojenieService} from './strojenie.service';

describe('StrojenieService', () => {
  let service: StrojenieService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(StrojenieService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
