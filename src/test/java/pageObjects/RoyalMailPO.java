package pageObjects;

import net.serenitybdd.core.annotations.findby.FindBy;
import net.serenitybdd.core.pages.PageObject;
import net.serenitybdd.core.pages.WebElementFacade;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.List;

public class RoyalMailPO extends PageObject {
    public static final String ROYALMAILPOSTCODEFINDERPAGETITLE = "Postcode Finder - Find an address | Royal Mail Group Ltd";

    @FindBy(id = "edit-rml-postcode-finder-postal-code")
    public WebElementFacade txtPostCode;

    @FindBy(xpath = "//button[@id='consent_prompt_submit']")
    public WebElementFacade btnConsentSubmit;

    @FindBy(className = "company")
    public WebElementFacade lblCompanyName;
    @FindBy(className = "line1")
    public WebElementFacade lblAddressLine1;
    @FindBy(className = "line2")
    public WebElementFacade lblAddressLine2;
    @FindBy(className = "line3")
    public WebElementFacade lblAddressLine3;

    @FindBy(className = "line4")
    public WebElementFacade lblAddressLine4;
    @FindBy(className = "city")
    public WebElementFacade lblCity;
    @FindBy(className = "postalcode")
    public WebElementFacade lblPostalCode;


    public List<WebElement> getAddressesElementListFromRM() {
        By lstAddressBy = By.cssSelector("div[class$='pcatext'] div[class='pca pcalist'] div");
        List<WebElement> addressListElements = getDriver().findElements(lstAddressBy);
        return addressListElements;
    }
}
