package stepDefinitions;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import net.serenitybdd.annotations.Steps;
import steps.QASSteps;

public class QASStepDef {

    @Steps
    QASSteps qasSteps;

    @Given("User is in RoyalMail PostCodeFinder Page")
    public void userIsInRoyalMailPostCodeFinderPage() {
        qasSteps.navigateToRoyalMailPostCodeFinderPage();
    }

    @Then("User validates the Retailer Address")
    public void userValidatesTheRetailerAddress() {
        qasSteps.validateRetailerAddressInRoyalMail();
    }
}
