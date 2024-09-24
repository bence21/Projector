import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { WordsSpellCheckerComponent } from './words-spell-checker.component';

describe('WordsSpellCheckerComponent', () => {
  let component: WordsSpellCheckerComponent;
  let fixture: ComponentFixture<WordsSpellCheckerComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ WordsSpellCheckerComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(WordsSpellCheckerComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
