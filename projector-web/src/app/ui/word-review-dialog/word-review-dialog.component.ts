import { Component, Inject, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material';
import { Language } from '../../models/language';

@Component({
  selector: 'app-word-review-dialog',
  templateUrl: './word-review-dialog.component.html',
  styleUrls: ['./word-review-dialog.component.css']
})
export class WordReviewDialogComponent implements OnInit {
  form: FormGroup;
  predefinedCategories = ['Dialectal', 'Archaic', 'Colloquial', 'Regional', 'Poetic license'];
  useCustomCategory = false;

  constructor(
    private dialogRef: MatDialogRef<WordReviewDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: { word: string; language: Language },
    private fb: FormBuilder
  ) {
  }

  ngOnInit() {
    this.createForm();
  }

  createForm() {
    this.form = this.fb.group({
      'category': ['', [Validators.required]],
      'customCategory': [''],
      'notes': ['']
    });
  }

  onCategoryChange() {
    const categoryValue = this.form.get('category').value;
    this.useCustomCategory = categoryValue === 'custom';
    if (this.useCustomCategory) {
      this.form.get('customCategory').setValidators([Validators.required]);
    } else {
      this.form.get('customCategory').clearValidators();
    }
    this.form.get('customCategory').updateValueAndValidity();
  }

  onCancel() {
    this.dialogRef.close();
  }

  onSave() {
    if (this.form.valid) {
      const category = this.useCustomCategory 
        ? this.form.get('customCategory').value 
        : this.form.get('category').value;
      this.dialogRef.close({
        category: category,
        notes: this.form.get('notes').value
      });
    }
  }
}
