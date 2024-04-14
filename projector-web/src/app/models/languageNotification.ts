import { BaseModel } from "./base-model";
import { Language } from "./language";
import { DayHourTimer } from "./dayHourTimer";

export class LanguageNotification extends BaseModel {
    language: Language;
    suggestions = true;
    newSongs = true;
    suggestionsDayHourTimer: DayHourTimer = new DayHourTimer();
    newSongsDayHourTimer: DayHourTimer = new DayHourTimer();
    private static ONE_DAY = 24 * 60 * 60 * 1000;
    suggestionsDelay = LanguageNotification.ONE_DAY;
    newSongsDelay = LanguageNotification.ONE_DAY;

    constructor(values: Object = {}) {
        super(values);
        Object.assign(this, values);
        this.suggestionsDayHourTimer = new DayHourTimer();
        this.suggestionsDayHourTimer.initialize(this.suggestionsDelay);
        this.newSongsDayHourTimer = new DayHourTimer();
        this.newSongsDayHourTimer.initialize(this.newSongsDelay);
    }
}