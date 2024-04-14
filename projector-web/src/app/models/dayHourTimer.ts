
export class DayHourTimer {
    delayDays = -1;
    delayHours = -1;
    delayMinutes = 15;

    constructor();
    constructor(values: Object = {}) {
        Object.assign(this, values);
    }

    initialize(delayMilliseconds: number) {
        if (delayMilliseconds != undefined) {
            this.delayMinutes = Math.round(delayMilliseconds / 1000 / 60);
            if (this.delayMinutes >= 60) {
                this.delayHours = Math.round(this.delayMinutes / 60);
                this.delayMinutes = -1;
                if (this.delayHours >= 24) {
                    this.delayDays = Math.round(this.delayHours / 24);
                    this.delayHours = -1;
                }
            }
        }
    }

    getInMilliseconds(): number {
        const minutesToMilliseconds = 60 * 1000;
        const hoursToMilliseconds = 60 * minutesToMilliseconds;
        const daysToMilliseconds = 24 * hoursToMilliseconds;
        if (this.delayDays != -1) {
            return this.delayDays * daysToMilliseconds;
        }
        if (this.delayHours != -1) {
            return this.delayHours * hoursToMilliseconds;
        }
        if (this.delayMinutes != -1) {
            return this.delayMinutes * minutesToMilliseconds;
        }
        return 0;
    }

    onMinutesChange() {
        if (this.delayMinutes >= 60) {
            this.delayHours = 1;
            this.delayMinutes = -1;
        }
    }

    onHoursChange() {
        if (this.delayHours >= 24) {
            this.delayDays = 1;
            this.delayHours = -1;
        } else if (this.delayHours == 0) {
            this.delayMinutes = 59;
            this.delayHours = -1;
        }
    }

    onDaysChange() {
        if (this.delayDays == 0) {
            this.delayHours = 23;
            this.delayDays = -1;
        }
    }
}