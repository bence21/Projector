export class BooleanResponse {
  response: boolean;

  constructor(values: Object = {}) {
    Object.assign(this, values);
  }
}