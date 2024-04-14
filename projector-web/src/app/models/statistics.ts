import {BaseModel} from './base-model';

export class Statistics extends BaseModel {

  nr: number;
  accessedDate = '';
  remoteAddress = '';
  uri = '';

  constructor(values: Object = {}) {
    super(values);
    Object.assign(this, values);
  }
}
