import { Component, Input } from '@angular/core';
import { SongCollection } from '../../models/songCollection';

@Component({
  selector: 'app-song-collection-lines',
  templateUrl: './song-collection-lines.component.html'
})
export class SongCollectionLinesComponent {
  @Input() collections: SongCollection[] = [];
}
