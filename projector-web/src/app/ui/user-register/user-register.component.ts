import { Component, OnInit, ViewChild, ElementRef } from '@angular/core';
import { FormGroup, FormBuilder, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { User } from '../../models/user';
import { UserDataService } from '../../services/user-data.service';
import { MatSnackBar } from '@angular/material';

@Component({
  selector: 'app-user-register',
  templateUrl: './user-register.component.html',
  styleUrls: ['./user-register.component.css']
})
export class UserRegisterComponent implements OnInit {

  @ViewChild('email', { static: false }) emailElement: ElementRef;

  userForm: FormGroup;
  model = new User();
  reEnteredPassword = '';
  firstName = '';
  surname = '';
  updating: boolean;

  formErrors = {
    'email': '',
    'surname': '',
    'firstName': '',
    'password': '',
    'reEnteredPassword': ''
  };

  validationMessages = {
    'email': {
      'required': 'Required',
      'pattern': 'Incorrect email'
    },
    'surname': {
      'required': 'Required'
    },
    'firstName': {
      'required': 'Required'
    },
    'password': {
      'required': 'Required',
      'minlength': 'Password-Minimum length is 5'
    },
    'reEnteredPassword': {
      'required': 'Required'
    }
  };

  constructor(private fb: FormBuilder,
    private userDataService: UserDataService,
    private snackBar: MatSnackBar,
    private router: Router,
  ) { }

  ngOnInit() {
    this.createForm();
  }

  onSubmit() {
    this.model.email = this.userForm.value.email;
    this.model.surname = this.userForm.value.surname;
    this.model.firstName = this.userForm.value.firstName;
    this.model.password = this.userForm.value.password;
    this.userDataService.addUser(this.model, 'en').subscribe(
      () => {
        this.router.navigate(['/login', this.model.email]);
      },
      (err) => {
        if (err.status.toString() === '409') {
          this.formErrors.email = 'Email is used!';
          this.emailElement.nativeElement.focus();
        } else {
          console.log(err);
          this.snackBar.open(err._body, 'Close', {
            duration: 5000
          })
        }
      }
    );
  }

  createForm() {
    this.userForm = this.fb.group({
      'email': [this.model.email, [
        Validators.required,
        Validators.pattern(/^(([^<>()\[\]\\.,;:\s@"]+(\.[^<>()\[\]\\.,;:\s@"]+)*)|(".+"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/)
      ]],
      'firstName': [this.firstName, [
        Validators.required
      ]],
      'surname': [this.surname, [
        Validators.required
      ]],
      'password': [this.model.password, [
        Validators.required,
        Validators.minLength(5)
      ]],
      'reEnteredPassword': [this.reEnteredPassword, [
        Validators.required
      ]]
    })
    this.userForm.valueChanges.subscribe(data => this.onValueChanged(data));
    this.onValueChanged();
  }

  onValueChanged(data?: any) {
    if (!this.userForm) {
      return;
    }
    const form = this.userForm;

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

    if ((data && data.hasOwnProperty('password') && data.hasOwnProperty('reEnteredPassword')) && data.password !== data.reEnteredPassword) {
      this.formErrors.reEnteredPassword = 'Password not match';
      form.get('reEnteredPassword').setErrors({ MatchPassword: true })
    } else {
      this.formErrors.reEnteredPassword = '';
      if (this.updating) {
        this.updating = false;
      } else {
        this.updating = true;
        form.get('reEnteredPassword').updateValueAndValidity();
      }
    }
  }
}
