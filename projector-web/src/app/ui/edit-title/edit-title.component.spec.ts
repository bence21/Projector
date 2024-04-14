import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { EditTitleComponent } from './edit-title.component';

describe('EditTitleComponent', () => {
  let component: EditTitleComponent;
  let fixture: ComponentFixture<EditTitleComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ EditTitleComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(EditTitleComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
