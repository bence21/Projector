import { Component, Inject, OnInit } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef, MatSnackBar } from '@angular/material';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { AuthService } from '../../services/auth.service';
import { User } from "../../models/user";

@Component({
  selector: 'app-authenticate',
  templateUrl: './authenticate.component.html',
  styleUrls: ['./authenticate.component.css']
})
export class AuthenticateComponent implements OnInit {

  loginForm: FormGroup;
  model = new User();

  formErrors = {
    'password': ''
  };
  validationMessages = {
    'password': {
      'required': 'password.PASSWORD_IS_REQUIRED'
    }
  };

  constructor(public dialogRef: MatDialogRef<AuthenticateComponent>,
    @Inject(MAT_DIALOG_DATA) public data: any,
    private fb: FormBuilder,
    private authService: AuthService,
    private snackBar: MatSnackBar) {
  }

  ngOnInit() {
    this.authService.loginConfirmed = false;
    this.model.email = this.data.email;
    this.loginForm = this.fb.group({
      'email': [this.model.email, [
        Validators.required,
        Validators.pattern(/^(([^<>()\[\]\\.,;:\s@"]+(\.[^<>()\[\]\\.,;:\s@"]+)*)|(".+"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/)
      ]],
      'password': [this.model.password, [
        Validators.required
      ]]
    });
    this.loginForm.valueChanges.subscribe(() => this.onValueChanged());
    this.onValueChanged();
    setInterval(() => {
      if (this.authService.loginConfirmed) {
        this.dialogRef.close('ok');
      }
    }, 100);
  }

  onValueChanged() {
    if (!this.loginForm) {
      return;
    }
    const form = this.loginForm;

    for (const field in this.formErrors) {
      if (this.formErrors.hasOwnProperty(field)) {
        this.formErrors[field] = '';
        const control = form.get(field);
        if (control && control.dirty && !control.valid) {
          const messages = this.validationMessages[field];
          for (const key in control.errors) {
            if (control.errors.hasOwnProperty(key)) {
              this.formErrors[field] = messages[key];
              break;
            }
          }
        }
      }
    }
  }

  onSubmit() {
    this.authService.login(this.loginForm.value.email, this.loginForm.value.password).subscribe(
      () => {
        this.authService.getUserFromServer().subscribe(
          (resp) => {
            const user = new User(resp);
            localStorage.setItem('currentUser', JSON.stringify(user));
            this.authService.setUser(user);
            this.authService.isLoggedIn = true;
            this.authService.loginConfirmed = true;
            this.dialogRef.close('ok');
          },
          () => {
            this.snackBar.open('Something wrong', undefined, {
              duration: 3000
            });
          }
        );
      },
      () => {
        this.authService.isLoggedIn = false;
        this.authService.setUser(null);
        this.snackBar.open('Something wrong', undefined, {
          duration: 3000
        });
      }
    );
  }

}