import { Component, Inject, OnInit } from '@angular/core';
import { Song } from "../../services/song-service.service";
import { SongCollectionDataService } from "../../services/song-collection-data.service";
import { SongCollection, SongCollectionElement } from "../../models/songCollection";
import { FormBuilder, FormGroup, Validators } from "@angular/forms";
import { MAT_DIALOG_DATA, MatDialogRef, MatDialog } from "@angular/material";
import { AuthenticateComponent } from '../authenticate/authenticate.component';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-add-to-collection',
  templateUrl: './add-to-collection.component.html',
  styleUrls: ['./add-to-collection.component.css']
})
export class AddToCollectionComponent implements OnInit {

  form: FormGroup;
  song: Song;
  songCollections: SongCollection[];
  selectedSongCollection: SongCollection;
  songCollectionElement: SongCollectionElement;

  constructor(
    public dialogRef: MatDialogRef<AddToCollectionComponent>,
    @Inject(MAT_DIALOG_DATA) public data: any,
    private fb: FormBuilder,
    private songCollectionDataService: SongCollectionDataService,
    private dialog: MatDialog,
    private auth: AuthService,
  ) {
  }

  ngOnInit() {
    this.song = this.data.song;
    this.songCollections = JSON.parse(localStorage.getItem("songCollections"));
    this.songCollectionDataService.getAllByLanguageMinimal(this.song.languageDTO).subscribe(songCollections => {
      songCollections.sort((songCollection1, songCollection2) => {
        if (songCollection1.modifiedDate < songCollection2.modifiedDate) {
          return 1;
        }
        if (songCollection1.modifiedDate > songCollection2.modifiedDate) {
          return -1;
        }
        return 0;
      });
      this.songCollections = songCollections;
      localStorage.setItem("songCollections", JSON.stringify(songCollections));
    });
    this.form = this.fb.group({
      'ordinalNumber': ['', [
        Validators.required,
      ]],
    });
  }

  onSubmit() {
    const formValue = this.form.value;
    this.songCollectionElement = new SongCollectionElement();
    this.songCollectionElement.ordinalNumber = formValue.ordinalNumber;
    this.songCollectionElement.songUuid = this.song.uuid;
    this.updateSongCollectionElement();
  }

  private updateSongCollectionElement() {
    const role = this.auth.getUser().getRolePath();
    this.songCollectionDataService.putInCollection(this.selectedSongCollection, this.songCollectionElement, role).subscribe(() => {
      this.dialogRef.close('ok');
      window.location.reload();
    }, (err) => {
      if (err.status === 405) {
        this.openAuthenticateDialog();
      }
      else {
        console.log(err);
      }
    });
  }

  private openAuthenticateDialog() {
    let user = JSON.parse(localStorage.getItem('currentUser'));
    const dialogRef = this.dialog.open(AuthenticateComponent, {
      data: {
        email: user.email
      }
    });

    dialogRef.afterClosed().subscribe((result) => {
      if (result === 'ok') {
        this.updateSongCollectionElement();
      }
    });
  }

  containingSongCollections(): boolean {
    return this.songCollections != undefined && this.songCollections.length > 0;
  }
}
