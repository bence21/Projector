import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatDialog } from '@angular/material';
import { Title } from '@angular/platform-browser';
import { Router } from '@angular/router';
import { Language } from '../../models/language';
import { SongCollection } from '../../models/songCollection';
import { LanguageDataService } from '../../services/language-data.service';
import { SongCollectionDataService } from '../../services/song-collection-data.service';
import { checkAuthenticationError } from '../../util/error-util';

@Component({
  selector: 'app-new-song-collection',
  templateUrl: './new-song-collection.component.html',
  styleUrls: ['./new-song-collection.component.css']
})
export class NewSongCollectionComponent implements OnInit {

  form: FormGroup;
  formErrors = {
    'title': '',
  };
  languages: Language[];
  selectedLanguage: Language = null;
  songCollection: SongCollection;

  constructor(
    private fb: FormBuilder,
    private songCollectionDataService: SongCollectionDataService,
    private languageDataService: LanguageDataService,
    private titleService: Title,
    private router: Router,
    private dialog: MatDialog,
  ) { }

  ngOnInit() {
    this.titleService.setTitle('New song collection');
    this.languages = [];
    this.createForm();
    this.loadLanguages();
  }

  private loadLanguages() {
    this.languageDataService.getAll().subscribe(
      (languages) => {
        this.languages = languages;
      }
    );
  }

  private createForm() {
    this.songCollection = new SongCollection();
    this.form = this.fb.group({
      'title': [this.songCollection.name, [
        Validators.required
      ]],
    });
  }

  onSubmit() {
    const formValue = this.form.value;
    this.songCollection.name = formValue.title;
    if (this.selectedLanguage == null) {
      return;
    }
    this.songCollection.languageUuid = this.selectedLanguage.uuid;
    this.songCollectionDataService.create(this.songCollection).subscribe(
      (_songCollection) => {
        // noinspection JSIgnoredPromiseFromCall
        this.router.navigate(['/songs']);
      },
      (err) => {
        checkAuthenticationError(this.onSubmit, this, err, this.dialog);
      }
    );
  }

}
