import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { NewSongCollectionComponent } from './new-song-collection.component';

describe('NewSongCollectionComponent', () => {
  let component: NewSongCollectionComponent;
  let fixture: ComponentFixture<NewSongCollectionComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ NewSongCollectionComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(NewSongCollectionComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
