import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { VersionLinkListComponent } from './version-link-list.component';

describe('VersionLinkListComponent', () => {
  let component: VersionLinkListComponent;
  let fixture: ComponentFixture<VersionLinkListComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ VersionLinkListComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(VersionLinkListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
