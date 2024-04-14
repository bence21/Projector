import { Component, OnInit } from '@angular/core';
import { BehaviorSubject } from 'rxjs/BehaviorSubject';
import { Observable } from 'rxjs/Observable';
import { DataSource } from '@angular/cdk/table';
import { Router } from '@angular/router';
import { UserDataService } from '../../services/user-data.service';
import { User } from '../../models/user';
import { MatDialog } from "@angular/material";
import { AuthService } from "../../services/auth.service";
import { Title } from "@angular/platform-browser";
import { checkAuthenticationError } from '../../util/error-util';

export class UserDatabase {
  dataChange: BehaviorSubject<User[]> = new BehaviorSubject<User[]>([]);

  constructor(userList: User[]) {
    if (userList !== null) {
      const copiedData = this.data;
      let nr = 0;
      for (const user of userList) {
        user.nr = ++nr;
        copiedData.push(user);
        this.dataChange.next(copiedData);
      }

      this.dataChange.next(copiedData);
    }
  }

  get data(): User[] {
    return this.dataChange.value;
  }
}

export class UserDataSource extends DataSource<any> {
  constructor(private _userDatabase: UserDatabase) {
    super();
  }

  connect(): Observable<User[]> {
    return this._userDatabase.dataChange;
  }

  disconnect() {
  }
}

@Component({
  selector: 'app-users',
  templateUrl: './users.component.html',
  styleUrls: ['./users.component.css']
})
export class UsersComponent implements OnInit {

  userList: User[] = [];
  displayedColumns = ['Nr', 'email', 'surname', 'firstName', 'role', 'activated'];
  userDatabase: any;
  dataSource: UserDataSource | null;

  constructor(
    public router: Router,
    private userDataService: UserDataService,
    private titleService: Title,
    private auth: AuthService,
    private dialog: MatDialog) {
  }

  ngOnInit() {
    this.titleService.setTitle('Users');
    this.userDataService.getAll().subscribe(
      (userList) => {
        this.userList = userList;
        this.userDatabase = new UserDatabase(this.userList);
        this.dataSource = new UserDataSource(this.userDatabase);
      },
      (err) => {
        checkAuthenticationError(this.ngOnInit, this, err, this.dialog);
      }
    );
  }

  onClick(row) {
    const user = this.userList[row.nr - 1];
    // noinspection JSIgnoredPromiseFromCall
    this.router.navigate(['/admin/user/', user.uuid]);
  }

}
