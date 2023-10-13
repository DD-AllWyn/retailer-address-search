package hooks;

import com.allwyn.framework.SerenityScenario;
import io.cucumber.java.Before;
import net.serenitybdd.annotations.Steps;


public class TestSetup {
    static {
        SerenityScenario.readConfigurations();
    }

    @Before()
    public void testSetUp() {

    }
}
