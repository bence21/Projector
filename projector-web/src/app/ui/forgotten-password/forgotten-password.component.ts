import { Input, Component, OnInit, OnDestroy } from '@angular/core';
import { FormGroup, FormBuilder, Validators } from '@angular/forms';
import { Params, ActivatedRoute } from '@angular/router';
import { MatSnackBar } from '@angular/material';
import { UserDataService } from '../../services/user-data.service';

@Component({
  selector: 'app-forgotten-password',
  templateUrl: './forgotten-password.component.html',
  styleUrls: ['./forgotten-password.component.css']
})
export class ForgottenPasswordComponent implements OnInit, OnDestroy {

  @Input() emailInput: string;
  form: FormGroup;
  email = '';
  emailSent = false;
  submitted = false;

  formErrors = {
    'email': ''
  }

  validationMessages = {
    'email': {
      'required': 'Email is required',
      'pattern': 'Incorrect email'
    }
  }

  constructor(private fb: FormBuilder,
    private activatedRoute: ActivatedRoute,
    private userDataService: UserDataService,
    private snackBar: MatSnackBar,
  ) { }

  ngOnInit() {
    this.activatedRoute.queryParams.subscribe((params: Params) => {
      if (params) {
        const c = params['email'];
        if (c) {
          this.email = params['email'];
        }
      }
    });
    if (this.emailInput) {
      this.email = this.emailInput;
    }
    this.createForm();
  }

  ngOnDestroy(): void {
  }

  createForm() {
    this.form = this.fb.group({
      'email': [this.email, [
        Validators.required,
        Validators.pattern(/^(([^<>()\[\]\\.,;:\s@"]+(\.[^<>()\[\]\\.,;:\s@"]+)*)|(".+"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/)
      ]]
    })
    this.form.valueChanges.subscribe(data => this.onValueChanged());
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
    this.submitted = true;
    this.snackBar.open('Sending email with token link.', undefined, {
      duration: 3000
    });
    this.userDataService.requireForgottenToken(this.form.value.email)
      .subscribe(() => {
        this.emailSent = true;
      },
        () => {
          this.snackBar.open('We could not change your password. Please try again later!', undefined, {
            duration: 7000
          });
          this.submitted = false;
        }
      )
  }

  validAndNotSumbitted() {
    return (!this.form.valid) || (this.submitted) ? true : false;
  }
}
