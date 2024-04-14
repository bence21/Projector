import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { YoutubeIdCheckComponent } from './youtube-id-check.component';

describe('YoutubeIdCheckComponent', () => {
  let component: YoutubeIdCheckComponent;
  let fixture: ComponentFixture<YoutubeIdCheckComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ YoutubeIdCheckComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(YoutubeIdCheckComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
