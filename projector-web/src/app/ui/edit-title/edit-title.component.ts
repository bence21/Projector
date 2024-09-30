import { Component, Input, OnInit } from '@angular/core';
import { FormGroup } from '@angular/forms';
import { MatIconRegistry } from '@angular/material';
import { DomSanitizer } from '@angular/platform-browser';
import { Language } from '../../models/language';
import { replace, valueRefactorable } from '../new-song/new-song.component';
import { debounceTime } from 'rxjs/operators';

@Component({
  selector: 'app-edit-title',
  templateUrl: './edit-title.component.html',
  styleUrls: ['./edit-title.component.css']
})
export class EditTitleComponent implements OnInit {

  @Input()
  selectedLanguage: Language;
  @Input()
  form: FormGroup;
  refactorable: boolean = false;

  constructor(
    iconRegistry: MatIconRegistry,
    public sanitizer: DomSanitizer,
  ) {
    iconRegistry.addSvgIcon(
      'change_case',
      sanitizer.bypassSecurityTrustResourceUrl('assets/icons/change_case.svg'));
  }

  ngOnInit() {
    const titleControl = this.getTitleControl();
    if (titleControl != null) {
      titleControl.valueChanges.pipe(debounceTime(700)).subscribe(() => {
          this.refactorable = this.refactorableTitle();
      });
    }
  }

  private capitalize(s: string): string {
    try {
      return s.substring(0, 1).toUpperCase() + s.substring(1);
    } catch (error) {
      console.log(error);
      return s;
    }
  }

  private contractionWithApostropheOrHyphenation(splitWithDelimiters: string[], i: number): boolean {
    if (i < 2) {
      return false;
    }
    const s = splitWithDelimiters[i - 1];
    const regEx = new RegExp('[A-Za-z]*');
    return (s == "'") || (s == "’") || (s == "-") && regEx.test(splitWithDelimiters[i - 2]);
  }

  private notInExceptions(s: string): boolean {
    return s != 'a' &&
      s != 'an' &&
      s != 'and' &&
      s != 'as' &&
      s != 'by' &&
      s != 'but' &&
      s != 'for' &&
      s != 'in' &&
      s != 'into' &&
      s != 'nor' &&
      s != 'of' &&
      s != 'on' &&
      s != 'or' &&
      s != 'the' &&
      s != 'to' &&
      s != 'until' &&
      s != 'unto' &&
      s != 'up' &&
      s != 'upon' &&
      s != 'with' &&
      s != 'within' &&
      s != 'without';
  }

  private lastWord(splitWithDelimiters: string[], index: number): boolean {
    for (let i = index + 1; i < splitWithDelimiters.length; ++i) {
      const split = splitWithDelimiters[i];
      const regEx = new RegExp('^[A-Za-z]*$');
      if (regEx.test(split)) {
        return false;
      }
    }
    return true;
  }

  private splitWithDelimiters(s: string): string[] {
    const regEx = new RegExp('\\W+');
    const split = s.split(regEx);
    let strings = [];
    for (const aSplit of split) {
      const indexOf = s.indexOf(aSplit);
      const delimiter = s.substring(0, indexOf);
      s = s.substring(indexOf + aSplit.length);
      if (!(delimiter == '')) {
        strings.push(delimiter);
      }
      if (!(aSplit == '')) {
        strings.push(aSplit);
      }
    }
    if (!(s == '')) {
      strings.push(s);
    }
    return strings;
  }

  private changeCase(title: string): string {
    let s: string;
    if (this.selectedLanguage != undefined && this.selectedLanguage instanceof Language && this.selectedLanguage.isEnglish()) {
      s = this.changeEnglishCase(title);
    } else {
      s = title;
    }
    return this.capitalize(s);
  }

  private changeEnglishCase(title: string) {
    const splitWithDelimiters = this.splitWithDelimiters(title);
    let s = '';
    for (let i = 0; i < splitWithDelimiters.length; ++i) {
      const split = splitWithDelimiters[i];
      const s1 = split.toLowerCase();
      const s2 = this.capitalize(s1);
      if ((this.notInExceptions(s1) || this.lastWord(splitWithDelimiters, i)) && !this.contractionWithApostropheOrHyphenation(splitWithDelimiters, i)) {
        s += s2;
      } else {
        s += s1;
      }
    }
    return s;
  }

  titleCaseApplicable(): boolean {
    const value = this.getTitleValue();
    return value != this.changeCase(value);
  }

  private getTitleValue(): string {
    const formValue = this.form.value;
    for (const key in formValue) {
      const aKey = 'title';
      if (formValue.hasOwnProperty(key) && key.startsWith(aKey)) {
        return formValue[key];
      }
    }
    return '';
  }

  refactorTitleCase() {
    let newValue = this.changeCase(this.getTitleValue());
    this.setValue(newValue);
  }

  private getTitleControl() {
    const formValue = this.form.value;
    for (const key in formValue) {
      const aKey = 'title';
      if (formValue.hasOwnProperty(key) && key.startsWith(aKey)) {
        return this.form.controls[aKey];
      }
    }
    return null;
  }

  private setValue(newValue: string) {
    const titleControl = this.getTitleControl();
    if (titleControl != null) {
        titleControl.setValue(newValue);
        titleControl.updateValueAndValidity();
      }
  }

  getTitleCaseToolTip(): string {
    return 'Changes case to: ' + this.changeCase(this.getTitleValue());
  }

  private refactorableTitle(): boolean {
    return valueRefactorable(this.getTitleValue());
  }

  private getRefactoredTitleValue(): string {
    return replace(this.getTitleValue());
  }

  refactorTitle() {
    this.setValue(this.getRefactoredTitleValue());
  }

  getTitleCaseRefactorToolTip(): string {
    return 'Change to: ' + this.getRefactoredTitleValue();
  }
}

