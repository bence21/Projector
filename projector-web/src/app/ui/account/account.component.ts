import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatSnackBar } from '@angular/material';
import { MatDialog } from '@angular/material';
import { Title } from '@angular/platform-browser';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { UserDataService } from '../../services/user-data.service';
import { ErrorUtil, generalError } from '../../util/error-util';
import { currentUser } from '../../util/local-storage';

@Component({
  selector: 'app-account',
  templateUrl: './account.component.html',
  styleUrls: ['./account.component.css']
})
export class AccountComponent implements OnInit {

  userForm: FormGroup;
  showForm = false;
  email = '';
  firstName = '';
  surname = '';

  formErrors = {
    'surname': '',
    'firstName': '',
  };

  validationMessages = {
    'surname': {
      'required': 'Required'
    },
    'firstName': {
      'required': 'Required'
    },
  };

  constructor(
    private titleService: Title,
    private fb: FormBuilder,
    private userDataService: UserDataService,
    private snackBar: MatSnackBar,
    private auth: AuthService,
    private router: Router,
    private dialog: MatDialog,
  ) { }

  ngOnInit() {
    this.titleService.setTitle('Account');
    this.userDataService.getLoggedInUser();
    this.userDataService.getLoggedInUser().subscribe(
      (user) => {
        this.auth.setUserAlsoToLocalStorage(user);
        this.createForm();
        this.showForm = true;
      },
      (err) => {
        if (!ErrorUtil.errorIsNeededLogin(err)) {
          generalError(this.ngOnInit, this, err, this.dialog, this.snackBar);
        } else {
          const user = currentUser();
          this.auth.logout();
          if (user == null) {
            return;
          }
          this.router.navigate(['/login', user.email]);
        }
      }
    );
  }

  onSubmit() {
    const user = this.auth.getUser();
    user.surname = this.userForm.value.surname;
    user.firstName = this.userForm.value.firstName;
    this.userDataService.update(user).subscribe(
      (_updatedUser) => {
        this.ngOnInit();
        this.snackBar.open('Saved', 'Close', {
          duration: 1000
        });
      },
      (err) => {
        generalError(this.onSubmit, this, err, this.dialog, this.snackBar);
      }
    );
  }

  createForm() {
    const user = this.auth.getUser();
    this.email = user.email;
    this.surname = user.surname;
    this.firstName = user.firstName;
    this.userForm = this.fb.group({
      'email': [this.email, [
      ]],
      'firstName': [this.firstName, [
        Validators.required
      ]],
      'surname': [this.surname, [
        Validators.required
      ]],
    });
    this.userForm.valueChanges.subscribe(data => this.onValueChanged(data));
    this.onValueChanged();
  }

  onValueChanged(_data?: any) {
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
  }

  logout() {
    this.auth.logout();
    this.router.navigate(['/login']);
  }

}
