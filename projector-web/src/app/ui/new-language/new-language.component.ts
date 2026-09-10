import {Component, Inject, OnInit} from '@angular/core';
import {Language} from "../../models/language";
import {LanguageDataService} from "../../services/language-data.service";
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {MAT_DIALOG_DATA, MatDialog, MatDialogRef} from "@angular/material";
import {checkAuthenticationError} from "../../util/error-util";
import {NewLanguageDialogResult} from "../../util/language-dialog.util";

export {NewLanguageDialogResult} from "../../util/language-dialog.util";

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
              private languageDataService: LanguageDataService,
              private dialog: MatDialog) {
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
        if (err && err.status === 409) {
          try {
            const existing = new Language(err.json());
            this.dialogRef.close({ existing } as NewLanguageDialogResult);
          } catch (parseError) {
            this.formErrors.englishName = 'Language already exists';
          }
        } else {
          checkAuthenticationError(this.onSubmit, this, err, this.dialog);
        }
      }
    );
  }
}
