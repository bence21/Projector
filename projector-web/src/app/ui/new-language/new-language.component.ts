import {Component, Inject, OnInit} from '@angular/core';
import {Language} from "../../models/language";
import {LanguageDataService} from "../../services/language-data.service";
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material";

@Component({
  selector: 'app-new-language',
  templateUrl: './new-language.component.html',
  styleUrls: ['./new-language.component.css']
})
export class NewLanguageComponent implements OnInit {
  form: FormGroup;
  formErrors = {
    'englishName': '',
    'nativeName': '',
  };

  validationMessages = {
    'englishName': {
      'required': 'Required field',
    },
    'nativeName': {
      'required': 'Required field',
    },
  };

  constructor(private dialogRef: MatDialogRef<NewLanguageComponent>,
              @Inject(MAT_DIALOG_DATA) private data: any,
              private fb: FormBuilder,
              private languageDataService: LanguageDataService) {
  }

  ngOnInit() {
    this.createForm();
  }

  createForm() {
    this.form = this.fb.group({
      'englishName': ['', [
        Validators.required,
      ]],
      'nativeName': ['', [
        Validators.required,
      ]],
    });
    this.form.valueChanges.subscribe(() => this.onValueChanged());
    this.onValueChanged();
  }

  onValueChanged() {
    if (!this.form) {
      return;
    }
    const form = this.form;

    for (const field in this.formErrors) {
      if (this.formErrors.hasOwnProperty(field)) {
        this.formErrors[field] = '';
        const control = form.get(field);

        if (control && control.dirty && !control.valid) {
          const messages = this.validationMessages[field];
          for (const key in control.errors) {
            if (control.errors.hasOwnProperty(key)) {
              this.formErrors[field] += messages[key];
              break;
            }
          }
        }
      }
    }
  }

  onSubmit() {
    const formValue = this.form.value;
    const language = new Language();
    language.englishName = formValue.englishName;
    language.nativeName = formValue.nativeName;
    this.languageDataService.create(language).subscribe(
      () => {
        this.dialogRef.close('ok');
      },
      (err) => {
        console.log(err);
      }
    );
  }
}
