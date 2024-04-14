import { Component, OnInit } from '@angular/core';
import { MatDialogRef } from "@angular/material";
import { MobileOsTypeEnum } from '../../util/enums';

@Component({
  selector: 'app-open-in-app',
  templateUrl: './open-in-app.component.html',
  styleUrls: ['./open-in-app.component.css']
})
export class OpenInAppComponent implements OnInit {

  public link = '';

  constructor(private dialogRef: MatDialogRef<OpenInAppComponent>) {
    let mobileOsTypeString: string = localStorage.getItem("mobileOsType")
    let mobileOsType = MobileOsTypeEnum[mobileOsTypeString];
    if (mobileOsType == MobileOsTypeEnum.Android) {
      this.link = 'https://play.google.com/store/apps/details?id=com.bence.songbook&launch=true';
    } else if (mobileOsType == MobileOsTypeEnum.Ios) {
      this.link = 'https://apps.apple.com/us/app/songbook-christian/id1489053683?mt=8';
    }
  }

  ngOnInit() {
  }

  dontShowAgain() {
    localStorage.setItem("OpenInAppComponent_dontShow", "true");
    this.dialogRef.close('ok');
  }
}
