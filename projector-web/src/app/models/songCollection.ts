import {BaseModel} from "./base-model";

export class SongCollection extends BaseModel {

  modifiedDate: number;
  createdDate: number;
  uuid: '';
  languageUuid: '';
  name: '';
  songCollectionElements: SongCollectionElement[];

  constructor(values: Object = {}) {
    super(values);
    Object.assign(this, values);
  }
}

export class SongCollectionElement extends BaseModel {

  ordinalNumber: '';
  songUuid: '';
  songCollection: SongCollection;

  constructor(values: Object = {}) {
    super(values);
    Object.assign(this, values);
  }
}