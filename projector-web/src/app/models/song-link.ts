import { BaseModel } from './base-model';

export class SongLink extends BaseModel {
  songId1: string;
  songId2: string;
  title1: string;
  title2: string;
  createdDate: Date;
  modifiedDate: Date;
  applied: boolean;
  createdByEmail: string;
  nr: number;

  constructor(values: Object = {}) {
    super(values);
    Object.assign(this, values);
  }
}
