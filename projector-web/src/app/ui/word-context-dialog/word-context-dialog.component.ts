import { Component, Inject, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material';
import { Language } from '../../models/language';

@Component({
  selector: 'app-word-context-dialog',
  templateUrl: './word-context-dialog.component.html',
  styleUrls: ['./word-context-dialog.component.css']
})
export class WordContextDialogComponent implements OnInit {
  form: FormGroup;
  predefinedContextCategories = ['Religious', 'Poetry', 'Formal', 'Informal', 'Historical', 'Regional'];
  useCustomCategory = false;

  constructor(
    private dialogRef: MatDialogRef<WordContextDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: { word: string; language: Language },
    private fb: FormBuilder
  ) {
  }

  ngOnInit() {
    this.createForm();
  }

  createForm() {
    this.form = this.fb.group({
      'contextCategory': ['', [Validators.required]],
      'customContextCategory': [''],
      'contextDescription': [''],
      'notes': ['']
    });
  }

  onCategoryChange() {
    const categoryValue = this.form.get('contextCategory').value;
    this.useCustomCategory = categoryValue === 'custom';
    if (this.useCustomCategory) {
      this.form.get('customContextCategory').setValidators([Validators.required]);
    } else {
      this.form.get('customContextCategory').clearValidators();
    }
    this.form.get('customContextCategory').updateValueAndValidity();
  }

  onCancel() {
    this.dialogRef.close();
  }

  onSave() {
    if (this.form.valid) {
      const contextCategory = this.useCustomCategory 
        ? this.form.get('customContextCategory').value 
        : this.form.get('contextCategory').value;
      this.dialogRef.close({
        contextCategory: contextCategory,
        contextDescription: this.form.get('contextDescription').value,
        notes: this.form.get('notes').value
      });
    }
  }
}
