import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ReviewerStatisticsListComponent } from './reviewer-statistics-list.component';

describe('ReviewerStatisticsListComponent', () => {
  let component: ReviewerStatisticsListComponent;
  let fixture: ComponentFixture<ReviewerStatisticsListComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ReviewerStatisticsListComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ReviewerStatisticsListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
