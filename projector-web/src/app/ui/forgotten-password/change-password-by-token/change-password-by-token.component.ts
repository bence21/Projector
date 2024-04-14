import { ApiService } from '../../../services/api.service';
import { Component, OnInit, OnDestroy } from '@angular/core';
import { FormGroup, FormBuilder, Validators } from '@angular/forms';
import { ActivatedRoute, Params, Router } from '@angular/router';
import { Observable, Subscription } from 'rxjs/Rx';
import { UserDataService } from '../../../services/user-data.service';
import { MatSnackBar } from '@angular/material';
import { User } from '../../../models/user';

@Component({
  selector: 'app-change-password-by-token',
  templateUrl: './change-password-by-token.component.html',
  styleUrls: ['./change-password-by-token.component.css']
})
export class ChangePasswordByTokenComponent implements OnInit, OnDestroy {
  wasInfoAboutHurryUp = false;
  subscription: Subscription;
  wasQueryParamsSubscription = false;
  counter: Observable<number>;
  expiryDate: Date;

  form: FormGroup;
  email = '';
  password = '';
  reEnteredPassword = '';
  updating = false;
  token = '';
  passwordChanged = false;
  tokenInvalid = false;
  remainingMSeconds = 100000000;
  remainingTime: Date;

  formErrors = {
    'password': '',
    'reEnteredPassword': ''
  };

  validationMessages = {
    'password': {
      'required': 'Password is required',
      'minlength': 'Password minimum length'
    },
    'reEnteredPassword': {
      'required': 'Password is required'
    }
  };

  constructor(private fb: FormBuilder,
    private activatedRoute: ActivatedRoute,
    private apiService: ApiService,
    private userDataService: UserDataService,
    private router: Router,
    private snackBar: MatSnackBar,
  ) { }

  ngOnInit() {
    this.createForm();
    this.activatedRoute.queryParams.subscribe((params: Params) => {
      if (params && !this.wasQueryParamsSubscription) {
        this.wasQueryParamsSubscription = true;
        let c = params['email'];
        if (c) {
          this.email = params['email'];
        }
        c = params['token'];
        if (c) {
          this.token = c;
        }
        c = params['expiryDate'];
        if (c) {
          this.expiryDate = new Date();
          this.expiryDate.setTime(c);
          const now = new Date();
          this.apiService.getTime().subscribe((res: any) => {
            now.setTime(res._body);
            if (now.getTime() > this.expiryDate.getTime()) {
              this.tokenExpired();
            } else {
              this.remainingMSeconds = this.expiryDate.getTime() - now.getTime();
              this.remainingTime = new Date();
              this.remainingTime.setTime(this.remainingMSeconds);
              this.expiryDate = new Date();
              this.expiryDate.setTime(now.getTime() + this.remainingMSeconds);
            }
          })
          this.remainingMSeconds = this.expiryDate.getTime() - now.getTime();
          this.remainingTime = new Date();
          this.remainingTime.setTime(this.remainingMSeconds);
        }
      }
    });
    this.counter = Observable.interval(1000).map((x) => {
      this.remainingTime = new Date();
      this.remainingMSeconds -= 1000;
      this.remainingTime.setTime(this.remainingMSeconds);
      return x;
    });

    this.subscription = this.counter.subscribe(() => {
      if (this.remainingMSeconds < 0) {
        if (!this.tokenInvalid) {
          this.tokenExpired();
        }
        this.subscription.unsubscribe();
      }
    });

  }

  private tokenExpired() {
    this.tokenInvalid = true;
    this.snackBar.open('Token expired. Require a new password reset', undefined, {
      duration: 7000
    });
  }

  ngOnDestroy(): void {
    this.subscription.unsubscribe();
  }

  createForm() {
    this.form = this.fb.group({
      'password': [this.password, [
        Validators.required,
        Validators.minLength(5)
      ]],
      'reEnteredPassword': [this.reEnteredPassword, [
        Validators.required
      ]]
    })
    this.form.valueChanges.subscribe(data => this.onValueChanged(data));
    this.onValueChanged();
  }

  onValueChanged(data?: any) {
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

  onSubmit() {
    const user = new User();
    user.email = this.email;
    user.password = this.form.value.password;
    this.userDataService.resetPassword(this.token, user)
      .subscribe(() => {
        this.passwordChanged = true;
        this.snackBar.open('Password changed!', undefined, {
          duration: 3000
        });
        this.router.navigate(['/login', this.email]);
      },
        () => {
          this.snackBar.open(' We could not change your password. Please try again later!', undefined, {
            duration: 5000
          });
        }
      )
  }

  isNeedHurry() {
    if (this.remainingMSeconds > 0 && this.remainingMSeconds < 60000 * 5 && !this.tokenInvalid) {
      if (!this.wasInfoAboutHurryUp) {
        this.wasInfoAboutHurryUp = true;
        this.snackBar.open('Token is expiring', undefined, {
          duration: 12000
        });
      }
      return true;
    }
    return false;
  }
}
