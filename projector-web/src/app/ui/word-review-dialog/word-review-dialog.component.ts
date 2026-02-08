import { Component, Inject, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material';
import { Language } from '../../models/language';
import { ReviewedWordDataService } from '../../services/reviewed-word-data.service';
import { LanguageDataService } from '../../services/language-data.service';

@Component({
  selector: 'app-word-review-dialog',
  templateUrl: './word-review-dialog.component.html',
  styleUrls: ['./word-review-dialog.component.css']
})
export class WordReviewDialogComponent implements OnInit {
  form: FormGroup;
  predefinedCategories = ['Dialectal', 'Archaic', 'Colloquial', 'Regional', 'Poetic license', 'Foreign Language'];
  useCustomCategory = false;
  isForeignLanguage = false;
  detectedLanguages: Language[] = [];
  allLanguages: Language[] = [];
  languageOptions: Language[] = [];
  loadingDetected = false;

  readonly foreignLanguageTypes = [
    { value: 0, label: 'Borrowed (written in song language style)' },
    { value: 1, label: 'Foreign (OK in source language, not in song language)' }
  ];

  constructor(
    private dialogRef: MatDialogRef<WordReviewDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: { word: string; language: Language },
    private fb: FormBuilder,
    private reviewedWordDataService: ReviewedWordDataService,
    private languageDataService: LanguageDataService
  ) {
  }

  ngOnInit() {
    this.createForm();
    this.languageDataService.getAll().subscribe(langs => {
      this.allLanguages = langs;
      this.updateLanguageOptions();
    });
  }

  createForm() {
    this.form = this.fb.group({
      'category': ['', [Validators.required]],
      'customCategory': [''],
      'sourceLanguage': [null],
      'foreignLanguageType': [null],
      'notes': ['']
    });
  }

  onCategoryChange() {
    const categoryValue = this.form.get('category').value;
    this.useCustomCategory = categoryValue === 'custom';
    this.isForeignLanguage = categoryValue === 'Foreign Language';

    if (this.useCustomCategory) {
      this.form.get('customCategory').setValidators([Validators.required]);
    } else {
      this.form.get('customCategory').clearValidators();
    }
    this.form.get('customCategory').updateValueAndValidity();

    if (this.isForeignLanguage) {
      this.form.get('sourceLanguage').setValidators([Validators.required]);
      this.form.get('foreignLanguageType').setValidators([Validators.required]);
      this.loadDetectedLanguages();
    } else {
      this.form.get('sourceLanguage').clearValidators();
      this.form.get('foreignLanguageType').clearValidators();
      this.form.get('sourceLanguage').setValue(null);
      this.form.get('foreignLanguageType').setValue(null);
    }
    this.form.get('sourceLanguage').updateValueAndValidity();
    this.form.get('foreignLanguageType').updateValueAndValidity();
  }

  private loadDetectedLanguages() {
    this.loadingDetected = true;
    this.reviewedWordDataService.detectSourceLanguages(this.data.word, this.data.language).subscribe(
      detected => {
        this.detectedLanguages = detected;
        this.updateLanguageOptions();
        this.loadingDetected = false;
      },
      () => { this.loadingDetected = false; }
    );
  }

  private updateLanguageOptions() {
    if (this.detectedLanguages.length > 0) {
      const detectedIds = new Set(this.detectedLanguages.map(l => l.uuid));
      const rest = this.allLanguages.filter(l => !detectedIds.has(l.uuid));
      this.languageOptions = [...this.detectedLanguages, ...rest];
    } else {
      this.languageOptions = [...this.allLanguages];
    }
  }

  compareLanguages(l1: Language, l2: Language): boolean {
    return l1 && l2 && l1.uuid === l2.uuid;
  }

  getLanguageOptionLabel(lang: Language): string {
    if (!lang) {
      return '';
    }
    const label = lang.printLanguage ? lang.printLanguage() : (lang.englishName || lang.nativeName || '');
    const isDetected = this.detectedLanguages.some(d => d.uuid === lang.uuid);
    return isDetected ? label + ' (detected)' : label;
  }

  onCancel() {
    this.dialogRef.close();
  }

  onSave() {
    if (this.form.valid) {
      const category = this.useCustomCategory
        ? this.form.get('customCategory').value
        : this.form.get('category').value;
      const result: any = {
        category: category,
        notes: this.form.get('notes').value
      };
      if (this.isForeignLanguage) {
        result.sourceLanguage = this.form.get('sourceLanguage').value;
        result.foreignLanguageType = this.form.get('foreignLanguageType').value;
      }
      this.dialogRef.close(result);
    }
  }
}
