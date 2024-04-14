import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {CompareSongsComponent} from './compare-songs.component';

describe('CompareSongsComponent', () => {
  let component: CompareSongsComponent;
  let fixture: ComponentFixture<CompareSongsComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [CompareSongsComponent]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(CompareSongsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should be created', () => {
    expect(component).toBeTruthy();
  });
});
