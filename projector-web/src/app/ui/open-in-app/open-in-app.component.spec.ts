import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {OpenInAppComponent} from './open-in-app.component';

describe('OpenInAppComponent', () => {
  let component: OpenInAppComponent;
  let fixture: ComponentFixture<OpenInAppComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [OpenInAppComponent]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(OpenInAppComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
