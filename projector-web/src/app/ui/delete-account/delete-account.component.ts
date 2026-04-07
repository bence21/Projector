import { Component, OnInit } from '@angular/core';
import { Title } from '@angular/platform-browser';
import { Router } from '@angular/router';
import { MatDialog, MatSnackBar } from '@angular/material';
import { AuthService } from '../../services/auth.service';
import { UserDataService } from '../../services/user-data.service';
import { ErrorUtil, generalError } from '../../util/error-util';
import { currentUser } from '../../util/local-storage';

@Component({
  selector: 'app-delete-account',
  templateUrl: './delete-account.component.html',
  styleUrls: ['./delete-account.component.css']
})
export class DeleteAccountComponent implements OnInit {

  confirmationText = '';
  email = '';
  deleting = false;
  canDelete = false;

  constructor(
    private titleService: Title,
    private auth: AuthService,
    private userDataService: UserDataService,
    private snackBar: MatSnackBar,
    private dialog: MatDialog,
    private router: Router,
  ) { }

  ngOnInit() {
    this.titleService.setTitle('Delete account');
    this.auth.getUserFromLocalStorage();
    const user = currentUser();
    this.canDelete = user != null && user.email != null && user.email.length > 0;
    this.email = this.canDelete ? user.email : '';
  }

  deleteAccount() {
    if (!this.canDelete || this.confirmationText !== 'DELETE') {
      return;
    }
    this.deleting = true;
    this.userDataService.deleteAccount().subscribe(
      () => {
        this.auth.logout();
        this.deleting = false;
        this.snackBar.open('Account deleted', 'Close', {
          duration: 2000
        });
        this.router.navigate(['/login']);
      },
      (err) => {
        this.deleting = false;
        if (ErrorUtil.errorIsNeededLogin(err)) {
          this.snackBar.open('Please log in again before deleting your account.', 'Close', {
            duration: 4000
          });
        }
        generalError(this.deleteAccount, this, err, this.dialog, this.snackBar);
      }
    );
  }
}
