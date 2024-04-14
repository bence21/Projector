import {AuthService} from '../../services/auth.service';
import {Component, Input, OnDestroy, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {ActivatedRoute, NavigationExtras, Router} from '@angular/router';
import {Subscription} from 'rxjs/Subscription';
import {User} from '../../models/user';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent implements OnInit, OnDestroy {
  sub: Subscription;

  @Input()
  email: string;
  loginForm: FormGroup;
  model = new User();

  formErrors = {
    'email': '',
    'password': ''
  };
  validationMessages = {
    'email': {
      'required': 'emailing.EMAIL_IS_REQUIRED',
      'pattern': 'emailing.INCORRECT_EMAIL'
    },
    'password': {
      'required': 'password.PASSWORD_IS_REQUIRED'
    }
  };

  constructor(private fb: FormBuilder,
              private authService: AuthService,
              private router: Router,
              private route: ActivatedRoute) {
  }

  ngOnInit() {
    if (this.email) {
      this.model.email = this.email;
    } else {
      this.sub = this.route.params.subscribe(params => {
        if (params['email']) {
          this.model.email = params['email'];
        }
      });
    }
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
  }

  ngOnDestroy(): void {
    this.sub.unsubscribe();
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
            this.authService.isLoggedIn = true;
            const user = new User(resp);
            this.authService.setUserAlsoToLocalStorage(resp);
            let redirect = this.authService.redirectUrl;
            if (!redirect) {
              if (user != null) {
                redirect = '/songs';
              } else {
                redirect = '/';
              }
            } else {
              if (user.isAdmin() && redirect.startsWith('/user')) {
                redirect = '/songs';
              }
            }
            // Set our navigation extras object
            // that passes on our global query params and fragment
            const navigationExtras: NavigationExtras = {
              queryParamsHandling: 'preserve',
              preserveFragment: true
            };
            // Redirect the user
            this.router.navigate([redirect], navigationExtras);
          },
          (err) => {
            this.authService.setUser(null);
            if (err.status.toString().charAt(0) === '4') {
              // this.translate.get('serverResponse.EMAIL_OR_PASSW_INCORRECT').subscribe((resp: string) => {
              //   this.toastr.error(resp);
              // });
            } else {
              // this.translate.get('informationalMsg.SOMETHING_WRONG').subscribe((resp: string) => {
              //   this.toastr.error(resp);
              // });
            }
          }
        );
      },
      () => {
        this.authService.setUser(null);
        // this.translate.get('informationalMsg.NO_RESPONSE_FROM_SERVER').subscribe((res: string) => {
        //   this.toastr.error(res);
        // });
      }
    );
  }
}
