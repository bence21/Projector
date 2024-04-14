import { Component, OnInit, OnDestroy } from '@angular/core';
import { UserDataService } from '../../services/user-data.service';
import { MatSnackBar, MatDialog } from '@angular/material';
import { ActivatedRoute } from '@angular/router';
import { Subscription } from "rxjs/Subscription";
import { AuthService } from '../../services/auth.service';
import { checkAuthenticationError, ErrorUtil } from '../../util/error-util';

@Component({
  selector: 'app-activation',
  templateUrl: './activation.component.html',
  styleUrls: ['./activation.component.css']
})
export class ActivationComponent implements OnInit, OnDestroy {

  activated = 'Something wrong';
  private sub: Subscription;
  code: string;

  constructor(
    private activatedRoute: ActivatedRoute,
    private userDataService: UserDataService,
    private snackBar: MatSnackBar,
    private dialog: MatDialog,
    private auth: AuthService,
  ) { }

  ngOnInit() {
    this.sub = this.activatedRoute.params.subscribe(params => {
      if (params['code']) {
        this.code = params['code'];
        this.activate();
      }
    });
  }

  private activate() {
    this.userDataService.getLoggedInUser().subscribe(
      () => {
        this.userDataService.activate(this.code).subscribe((response) => {
          console.log(response);
          this.userDataService.getLoggedInUser().subscribe(
            (user) => {
              this.activated = 'Successfully activated';
              this.auth.setUserAlsoToLocalStorage(user);
            },
            (_err) => {
              this.activated = 'Activation failed';
            }
          );
        }, (err) => {
          this.activated = 'Precondition failed';
          this.handleError(err);
        });
      }, (err) => {
        this.handleError(err);
      }
    )
  }

  private handleError(err: any) {
    if (ErrorUtil.errorIsNeededLogin(err)) {
      checkAuthenticationError(this.activate, this, err, this.dialog);
    } else {
      console.log(err);
      this.snackBar.open(err._body, 'Close', {
        duration: 5000
      });
    }
  }

  ngOnDestroy(): void {
    this.sub.unsubscribe();
  }

}
