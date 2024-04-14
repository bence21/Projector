import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { YoutubeVideoCheckerComponent } from './youtube-video-checker.component';

describe('YoutubeVideoCheckerComponent', () => {
  let component: YoutubeVideoCheckerComponent;
  let fixture: ComponentFixture<YoutubeVideoCheckerComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ YoutubeVideoCheckerComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(YoutubeVideoCheckerComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
