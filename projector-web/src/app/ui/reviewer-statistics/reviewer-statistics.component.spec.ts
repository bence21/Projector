import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ReviewerStatisticsComponent } from './reviewer-statistics.component';

describe('ReviewerStatisticsComponent', () => {
  let component: ReviewerStatisticsComponent;
  let fixture: ComponentFixture<ReviewerStatisticsComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ReviewerStatisticsComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ReviewerStatisticsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
