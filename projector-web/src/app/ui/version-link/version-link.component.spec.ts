import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { VersionLinkComponent } from './version-link.component';

describe('VersionLinkComponent', () => {
  let component: VersionLinkComponent;
  let fixture: ComponentFixture<VersionLinkComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ VersionLinkComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(VersionLinkComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
