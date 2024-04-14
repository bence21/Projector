import {Component, Inject, OnInit} from '@angular/core';
import {MAT_DIALOG_DATA} from "@angular/material";

@Component({
  selector: 'app-share',
  templateUrl: './share.component.html',
  styleUrls: ['./share.component.css']
})
export class ShareComponent implements OnInit {

  copied = '';

  constructor(@Inject(MAT_DIALOG_DATA) public data: any,) {
  }

  private static select(copyText) {
    copyText.select();
  }

  ngOnInit() {
    let copyText = document.getElementById("link");
    ShareComponent.select(copyText);
  }

  copyLink() {
    let copyText = document.getElementById("link");
    ShareComponent.select(copyText);
    document.execCommand("Copy");
    this.copied = 'Content copied to clipboard!';
  }

  copyEmbedded() {
    let copyText = document.getElementById("embedded");
    ShareComponent.select(copyText);
    document.execCommand("Copy");
    this.copied = 'Content copied to clipboard!';
  }
}
