export class BaseModel {

  uuid;
  id: '';

  constructor(values: Object = {}) {
    Object.assign(this, values);
  }
}
