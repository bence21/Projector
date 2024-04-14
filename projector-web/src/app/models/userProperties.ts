import { BaseModel } from "./base-model";
import { LanguageNotification } from "./languageNotification";

export class UserProperties extends BaseModel {
    notificationsByLanguage: LanguageNotification[];

    constructor(values: Object = {}) {
        super(values);
        Object.assign(this, values);
    }
}