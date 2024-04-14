package projector.utils;

import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextFormatter;

import java.util.function.UnaryOperator;

public class IntegerFilter implements UnaryOperator<TextFormatter.Change> {
    private final SpinnerValueFactory.IntegerSpinnerValueFactory valueFactory;

    public IntegerFilter(SpinnerValueFactory.IntegerSpinnerValueFactory valueFactory) {
        this.valueFactory = valueFactory;
    }

    @Override
    public TextFormatter.Change apply(TextFormatter.Change change) {
        // If the proposed change is a valid integer, allow it
        try {
            int newValue = Integer.parseInt(change.getControlNewText());
            if (isValidValue(newValue)) {
                return change;
            }
        } catch (NumberFormatException ignored) {
            // Ignore the exception for non-integer input
        }

        // Reject the change for invalid input
        return null;
    }

    private boolean isValidValue(int newValue) {
        // Manually check if the new value is within the specified range
        return newValue >= valueFactory.getMin() && newValue <= valueFactory.getMax();
    }
}
