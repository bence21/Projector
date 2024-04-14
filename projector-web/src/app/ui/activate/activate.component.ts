import { Component, OnInit } from '@angular/core';
import { UserDataService } from '../../services/user-data.service';
import { MatDialog, MatSnackBar } from '@angular/material';
import { checkAuthenticationError, ErrorUtil } from '../../util/error-util';

@Component({
  selector: 'app-activate',
  templateUrl: './activate.component.html',
  styleUrls: ['./activate.component.css']
})
export class ActivateComponent implements OnInit {

  resendActivationEmailEnabled = true;

  constructor(
    private userDataService: UserDataService,
    private snackBar: MatSnackBar,
    private dialog: MatDialog,
  ) { }

  ngOnInit() {
  }

  resendActivationEmail() {
    this.resendActivationEmailEnabled = false;
    this.userDataService.resendActivation().subscribe(() => {
    }, (err) => {
      if (ErrorUtil.errorIsNeededLogin(err)) {
        checkAuthenticationError(this.resendActivationEmail, this, err, this.dialog);
      } else {
        console.log(err);
        this.snackBar.open(err._body, 'Close', {
          duration: 5000
        });
      }
    });
  }

}
