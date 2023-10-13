package steps;

import com.allwyn.framework.utilities.reports.SerenityReport;
import com.allwyn.framework.utilities.webElements.UIButton;
import com.allwyn.framework.utilities.webElements.UILabel;
import com.allwyn.framework.utilities.webElements.UITextBox;
import net.serenitybdd.annotations.Steps;

public class CommonSteps {
    @Steps
    protected SerenityReport serenityReport;
    @Steps
    protected UIButton uiButton;
    @Steps
    protected UITextBox uiTextBox;

    @Steps
    protected UILabel uiLabel;
}
