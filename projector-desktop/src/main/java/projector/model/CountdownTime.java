package projector.model;

import com.j256.ormlite.field.DatabaseField;
import projector.controller.util.AutomaticAction;

import java.util.Date;

import static projector.controller.UtilsController.getDateByTimeText;

public class CountdownTime extends BaseEntity {

    @DatabaseField
    private String timeText;
    @DatabaseField
    private Long counter;
    @DatabaseField
    private AutomaticAction selectedAction;
    @DatabaseField
    private Boolean showFinishTime;
    @DatabaseField
    private String selectedProjectionScreenName;

    public String getTimeText() {
        return timeText;
    }

    public void setTimeText(String timeText) {
        this.timeText = timeText;
    }

    public long getCounter() {
        if (counter == null) {
            return 0;
        }
        return counter;
    }

    public void setCounter(long counter) {
        this.counter = counter;
    }

    public AutomaticAction getSelectedAction() {
        return selectedAction;
    }

    public void setSelectedAction(AutomaticAction selectedAction) {
        this.selectedAction = selectedAction;
    }

    public void setShowFinishTime(boolean showFinishTime) {
        this.showFinishTime = showFinishTime;
    }

    public boolean isShowFinishTime() {
        return showFinishTime != null && showFinishTime;
    }

    public void setSelectedProjectionScreenName(String selectedProjectionScreenName) {
        this.selectedProjectionScreenName = selectedProjectionScreenName;
    }

    public String getSelectedProjectionScreenName() {
        return selectedProjectionScreenName;
    }

    public double getScore(Date now, long maximum, long maximumCounter) {
        Date dateByTimeText = getDate();
        double dateScore = 0;
        if (dateByTimeText != null && dateByTimeText.after(now) && maximum != 0) {
            dateScore = dateByTimeText.getTime() - now.getTime();
            dateScore /= maximum;
        }
        double counter = getCounter();
        counter /= maximumCounter;
        return counter - dateScore;
    }

    public Date getDate() {
        return getDateByTimeText(getTimeText());
    }
}
