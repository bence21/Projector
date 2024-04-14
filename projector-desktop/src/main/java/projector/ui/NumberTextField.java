package projector.ui;


import javafx.scene.control.TextField;

import static java.lang.Math.round;

public class NumberTextField extends TextField {

    private String getDoubleDisplay(double x) {
        long precision = 1000L;
        double v = x * precision;
        long rounded = round(v);
        if (Math.abs(rounded) == 0) {
            return "0";
        }
        double v1 = (double) rounded / precision;
        long l = round(v1);
        if (l * precision == round(x * precision)) {
            return l + "";
        }
        return x + "";
    }

    public void setValue(double x) {
        setText(getDoubleDisplay(x));
    }
}
