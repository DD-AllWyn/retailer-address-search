package steps;

import com.allwyn.Excel;
import com.allwyn.framework.SerenityScenario;
import com.codoid.products.fillo.Connection;
import com.codoid.products.fillo.Fillo;
import com.codoid.products.fillo.Recordset;
import dataObject.RetailerAddress;
import me.xdrop.fuzzywuzzy.FuzzySearch;
import net.serenitybdd.annotations.Step;
import net.serenitybdd.annotations.Steps;
import org.openqa.selenium.WebElement;
import pageObjects.RoyalMailPO;

import java.util.Arrays;
import java.util.List;

import static net.serenitybdd.core.Serenity.*;

public class QASSteps extends CommonSteps {

    @Steps
    RoyalMailPO royalMailPO;

    boolean alreadyClicked = false;
    boolean alreadyMatched = false;

    String refcode = " ";

    @Step("Royal Mail Post Code Finder Page Title is verified")
    public void navigateToRoyalMailPostCodeFinderPage() {
        try {
            String royalMailPostCodeFinderURL = SerenityScenario.configProp.getProperty("royalMailPostCodeFinderURL");
            getDriver().get(royalMailPostCodeFinderURL);
            serenityReport.logStandardSerenityReport("User navigated to the Royal Mail Post Code Finder Page");
            String webPageTitle = getDriver().getTitle();
            serenityReport.logTestValidationReport("Web Page Title '" + RoyalMailPO.ROYALMAILPOSTCODEFINDERPAGETITLE + "' is as expected", "Web Page Title does not match.", webPageTitle.equalsIgnoreCase(RoyalMailPO.ROYALMAILPOSTCODEFINDERPAGETITLE));
            uiButton.clickButton(royalMailPO.btnConsentSubmit);
        } catch (Exception Ex) {
            Ex.printStackTrace();
        }
    }

    @Step("1. Retailer Address verification in Royal Mail WebSite")
    public void validateRetailerAddressInRoyalMail() {
        try {
            String excelWorkBookName = SerenityScenario.configProp.getProperty("addressInputExcelWorkBook");
            String excelDataSheetName = SerenityScenario.configProp.getProperty("addressInputDataSheet");

            //GET THE ROWCOUNT FROM THE ADDRESS DATA SHEET
            int addressRecordCount = Excel.getRowCountBySheetName(excelWorkBookName, excelDataSheetName);

            for (int dataRowIter = 1; dataRowIter <= addressRecordCount; dataRowIter++) {
                //GET THE ADDRESS DATA ROW BY ROW (FILTER THE DATA BY ROW NO)
                Recordset addressRecord = Excel.getFilteredDataFromExcelSheet(excelWorkBookName, excelDataSheetName, "RowNo", String.valueOf(dataRowIter));

                //CREATE ADDRESS INSTANCE
                RetailerAddress retailerAddress = new RetailerAddress(dataRowIter, addressRecord);

                //SEARCH FOR RETAILER ADDRESS IN ROYAL MAIL
                searchForRetailerAddress(retailerAddress);

            }
        } catch (Exception Ex) {
            Ex.printStackTrace();
        }
    }

    @Step("2. Address Search at Royal Mail Post Code Finder")
    private void searchForRetailerAddress(RetailerAddress prmRetailerAddress) {

        try {
            //ENTER THE ADDRESS MAIL IN THE POST CODE FINDER TEXT BOX
            //ENTER THE DETAILS AFTER SOME DELAY SO ROYALMAIL CAN PULL THE MATCHING ADDRESS
            royalMailPO.txtPostCode.clear();
            royalMailPO.txtPostCode.sendKeys(prmRetailerAddress.getRetailerName() + " ");
            Thread.sleep(200);
            royalMailPO.txtPostCode.sendKeys(prmRetailerAddress.getStreet() + " ");
            Thread.sleep(200);
            royalMailPO.txtPostCode.sendKeys(prmRetailerAddress.getDistrict() + " ");
            Thread.sleep(200);
            royalMailPO.txtPostCode.sendKeys(prmRetailerAddress.getCity() + " ");
            Thread.sleep(200);
            royalMailPO.txtPostCode.sendKeys(prmRetailerAddress.getPostCode());
            Thread.sleep(1000);

            //ANALYSE THE SUGGESTED ADDRESS IN THE DROPDOWN LIST AND LOOK FOR THE NEARLY MATCHING ADDRESS
            analyseTheSuggestedAddressListInRM(prmRetailerAddress);

        } catch (Exception Ex) {
            Ex.printStackTrace();
        }
    }

    @Step("3. Analysing the Suggested Address from Royal Mails")
    private void analyseTheSuggestedAddressListInRM(RetailerAddress prmRetailerAddress) {
        try {
            List<WebElement> lstMatchingAddressesFromRM = royalMailPO.getAddressesElementListFromRM();
            //FOR EACH SUGGESTED ADDRESS, APPLY THE LOGIC
            ////////////ADD MORE COMMENTS LATER/////////////////
            for (int addrIter = 0; addrIter < lstMatchingAddressesFromRM.size(); addrIter++) {

                String addressFromRM = lstMatchingAddressesFromRM.get(addrIter).getText().toLowerCase().replaceAll(",", "");
                System.out.println("ADDRESS " + (addrIter + 1) + " FROM LIST = " + addressFromRM);

                String addressFromPMTrimmed = lstMatchingAddressesFromRM.get(addrIter).getText().toLowerCase().replaceAll("^.*?,(.*),.*", "$1");
                System.out.println("TRIMMED ADDRESS " + (addrIter + 1) + " FROM LIST = " + addressFromPMTrimmed);

                String combinedAddress = prmRetailerAddress.getModifiedStreet() + " " + prmRetailerAddress.getDistrict() + " " + prmRetailerAddress.getCity();

                //IF THERE ARE MORE ADDRESSES THAT MATCH WITH THE COMPLETE POST CODE, SELECT THE ADDRESS AND ANALYSE THE NEXT SET OF ADDRESS THAT ROYAL MAIL SUGGESTS
                if (addressFromRM.contains(prmRetailerAddress.getModifiedStreet() + prmRetailerAddress.getCityFragments()[0]) || addressFromRM.contains(prmRetailerAddress.getModifiedStreet() + prmRetailerAddress.getDistrict()) || FuzzySearch.ratio(addressFromPMTrimmed, combinedAddress) > 90) {
                    lstMatchingAddressesFromRM.get(addrIter).click();
                    System.out.println("postcode + more addresses");
                    Thread.sleep(1000);
                    //CALL THE FUNCTION TO ANALYSE THE SET OF MORE-ADDRESSES AND FIND ADDRESS THAT NEARLY MATCHES RETAILER NAME
                    getTheAddressMatchingRetailerName(prmRetailerAddress);
                    Thread.sleep(2000);
                    royalMailPO.txtPostCode.clear();
                    break;
                }
                //IF THERE ARE MORE ADDRESSES THAT MATCH WITH THE PARTIAL POST CODE, SELECT THE ADDRESS AND ANALYSE THE NEXT SET OF ADDRESS THAT ROYAL MAIL SUGGESTS
                else if (addressFromRM.contains(prmRetailerAddress.getPostCodeFragments()[0] + " - more addresses")) {
                    lstMatchingAddressesFromRM.get(addrIter).click();
                    System.out.println("partial postcode + more addresses");
                    Thread.sleep(1000);
                    //CALL THE FUNCTION TO ANALYSE THE SET OF MORE-ADDRESSES AND FIND ADDRESS THAT NEARLY MATCHES RETAILER NAME
                    getTheAddressMatchingRetailerName(prmRetailerAddress);
                    Thread.sleep(2000);
                    royalMailPO.txtPostCode.clear();

                    break;
                }
                //IF THERE ARE MORE ADDRESSES THAT MATCH WITH THE COMPLETE POST CODE AND STREET NUMBER, SELECT THE ADDRESS AND ANALYSE THE NEXT SET OF ADDRESS THAT ROYAL MAIL SUGGESTS
                else if (addressFromRM.contains(prmRetailerAddress.getPostCode()) && (addressFromRM.contains(prmRetailerAddress.getStreetNo1() + " " + prmRetailerAddress.getModifiedStreet()) || (addressFromRM.contains(prmRetailerAddress.getStreetNo2() + " " + prmRetailerAddress.getModifiedStreet()) && prmRetailerAddress.getStreetNo2() != "") || addressFromRM.matches(".*?" + prmRetailerAddress.getStreetNo1() + "-" + "\\d+\\s+" + prmRetailerAddress.getModifiedStreet() + ".*") || addressFromRM.matches(".*?" + "\\d+" + "-" + prmRetailerAddress.getStreetNo1() + "\\s+" + prmRetailerAddress.getModifiedStreet() + ".*") || addressFromRM.matches(".*?" + prmRetailerAddress.getStreetNo2() + "-" + "\\d+\\s+" + prmRetailerAddress.getModifiedStreet() + ".*") || addressFromRM.matches(".*?" + "\\d+" + "-" + prmRetailerAddress.getStreetNo2() + "\\s+" + prmRetailerAddress.getModifiedStreet() + ".*"))) {
                    lstMatchingAddressesFromRM.get(addrIter).click();
                    System.out.println("Street number match and full postcode match");
                    Thread.sleep(2000);
                    //UPDATE THE ADDRESS-DATA SHEET WITH THE INFORMATION DISPLAYED IN ROYAL MAIL
                    updateRetailerDetailsInDataSheet(prmRetailerAddress);
                    Thread.sleep(2000);
                    royalMailPO.txtPostCode.clear();
                    break;
                }
                //IF THERE ARE MORE ADDRESSES THAT MATCH WITH THE COMPLETE POST CODE
                //BUT THERE IS NO MATCHING STREET NUMBER
                //REPORT THE MISMATCHING INFORMATION
                else if (addressFromRM.contains(prmRetailerAddress.getPostCode()) && (addressFromRM.contains(prmRetailerAddress.getStreetNo1() + " " + prmRetailerAddress.getModifiedStreet()) || (addressFromRM.contains(prmRetailerAddress.getStreetNo2() + " " + prmRetailerAddress.getModifiedStreet()) && prmRetailerAddress.getStreetNo2() != "") || addressFromRM.matches(".*?" + prmRetailerAddress.getStreetNo1() + "-" + "\\d+\\s+" + prmRetailerAddress.getModifiedStreet() + ".*") || addressFromRM.matches(".*?" + "\\d+" + "-" + prmRetailerAddress.getStreetNo1() + "\\s+" + prmRetailerAddress.getModifiedStreet() + ".*") || addressFromRM.matches(".*?" + prmRetailerAddress.getStreetNo2() + "-" + "\\d+\\s+" + prmRetailerAddress.getModifiedStreet() + ".*") || addressFromRM.matches(".*?" + "\\d+" + "-" + prmRetailerAddress.getStreetNo2() + "\\s+" + prmRetailerAddress.getModifiedStreet() + ".*")) == false) {
                    lstMatchingAddressesFromRM.get(addrIter).click();
                    System.out.println("Postcode match but Street number does not match");
                    Thread.sleep(2000);
                    updateRetailerDetailsInDataSheet(prmRetailerAddress);
                    Thread.sleep(2000);
                    //UPDATE THE ADDRESS-DATA SHEET WITH THE STREET NUMBER MISMATCH COMMENT
                    updateCommentOnStreet("Street number does not match");
                    royalMailPO.txtPostCode.clear();
                    break;
                } else {
                    System.out.println("POST CODE DOESNOT MATCH");
                }
            }
        } catch (Exception Ex) {
            Ex.printStackTrace();
        }
    }

    @Step("4. Find the Address  with nearly matching Retailer Name from Royal Mails")
    private void getTheAddressMatchingRetailerName(RetailerAddress prmRetailerAddress) {

        List<WebElement> lstMatchingAddress = royalMailPO.getAddressesElementListFromRM();

        try {
            Thread.sleep(1000);
            //FOR EACH SUGGESTED ADDRESS IN THE 'MORE-ADDRESS' LIST, APPLY THE 'RETAILER NAME MATCHING' LOGIC
            for (int iter = 0; iter < lstMatchingAddress.size(); iter++) {

                String addressFromList = lstMatchingAddress.get(iter).getText().toLowerCase().replaceAll("'", "");
                System.out.println("MATCHING ADDRESS FROM MORE ADDRESS=== " + addressFromList);
                String[] addressFromListSplit = addressFromList.split(",");
                String retailNameFromAddress = addressFromListSplit[0];
                Thread.sleep(1000);
                System.out.println(retailNameFromAddress);

                String[] retailerNameSplit = prmRetailerAddress.getRetailerNameFragments();
                //SPLIT THE RETAILER NAME AND FOR EACH SPLIT, FIND THE MATCH IN THE ROYALMAIL-SUGGESTED ADDRESS LIST
                for (int nameSplitIter = 0; nameSplitIter < retailerNameSplit.length; nameSplitIter++) {

                    if (retailerNameSplit[nameSplitIter].length() > 1 && retailNameFromAddress.contains(retailerNameSplit[nameSplitIter]) && !Arrays.asList("and", "the", "store", "stores", "shop", "ltd").contains(retailerNameSplit[nameSplitIter]) && retailerNameSplit[nameSplitIter].contains(prmRetailerAddress.getStreet()) == false && (retailerNameSplit[nameSplitIter].contains(prmRetailerAddress.getDistrict()) == false || prmRetailerAddress.getDistrict() == "") && retailerNameSplit[nameSplitIter].contains(prmRetailerAddress.getCity()) == false && addressFromList.contains(prmRetailerAddress.getPostCode())) {
                        Thread.sleep(1000);
                        lstMatchingAddress.get(iter).click();
                        Thread.sleep(1000);
                        //UPDATE THE ADDRESS-DATA SHEET WITH THE INFORMATION DISPLAYED IN ROYAL MAIL
                        updateRetailerDetailsInDataSheet(prmRetailerAddress);
                        Thread.sleep(2000);
                        alreadyClicked = true;
                        break;
                    }
                }

                if (alreadyClicked == false && (retailNameFromAddress.contains(prmRetailerAddress.getFramedRetailerName().replaceAll("s$", "")) || (prmRetailerAddress.getRetailerName().contains(prmRetailerAddress.getTempRetailerName()) && retailNameFromAddress.contains(prmRetailerAddress.getAlternateRetailerName1())) || (prmRetailerAddress.getRetailerName().contains(prmRetailerAddress.getTempRetailerName()) && retailNameFromAddress.contains(prmRetailerAddress.getAlternateRetailerName2()))) && addressFromList.contains(prmRetailerAddress.getPostCode())) {
                    Thread.sleep(1000);
                    lstMatchingAddress.get(iter).click();
                    Thread.sleep(1000);
                    //UPDATE THE ADDRESS-DATA SHEET WITH THE INFORMATION DISPLAYED IN ROYAL MAIL
                    updateRetailerDetailsInDataSheet(prmRetailerAddress);
                    Thread.sleep(2000);
                    alreadyClicked = true;
                    break;
                }
                if (alreadyClicked == true) {
                    break;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Step("5. Retailer Address Details update in DataSheet")
    private void updateRetailerDetailsInDataSheet(RetailerAddress prmRetailerAddress) {
        String retailerNameAsDisplayedInRM = royalMailPO.lblCompanyName.getText();
        String addressLine1AsDisplayedInRM = royalMailPO.lblAddressLine1.getText();
        String addressLine2AsDisplayedInRM = royalMailPO.lblAddressLine2.getText();
        String addressLine3AsDisplayedInRM = royalMailPO.lblAddressLine3.getText();
        String addressLine4AsDisplayedInRM = royalMailPO.lblAddressLine4.getText();
        String cityAsDisplayedInRM = royalMailPO.lblCity.getText();
        String postCodeAsDisplayedInRM = royalMailPO.lblPostalCode.getText();

        try {
            String retailerNameWithoutApostrophe = retailerNameAsDisplayedInRM.replaceAll("'", "").toLowerCase();
            if ((retailerNameWithoutApostrophe.contains(prmRetailerAddress.getFramedRetailerName()) && prmRetailerAddress.getFramedRetailerName().contains(prmRetailerAddress.getStreet()) == false && (prmRetailerAddress.getFramedRetailerName().contains(prmRetailerAddress.getDistrict()) == false || prmRetailerAddress.getDistrict() == "") && prmRetailerAddress.getFramedRetailerName().contains(prmRetailerAddress.getCity()) == false) || (prmRetailerAddress.getRetailerName().contains(prmRetailerAddress.getTempRetailerName()) && retailerNameWithoutApostrophe.contains(prmRetailerAddress.getAlternateRetailerName1())) || (prmRetailerAddress.getRetailerName().contains(prmRetailerAddress.getTempRetailerName()) && retailerNameWithoutApostrophe.contains(prmRetailerAddress.getAlternateRetailerName2()))) {
                if (!(postCodeAsDisplayedInRM.equals(refcode))) {
                    updateCompanyName(retailerNameAsDisplayedInRM);
                    updateAddrLine1(addressLine1AsDisplayedInRM);
                    updateCity(cityAsDisplayedInRM);
                    updatePostalCode(postCodeAsDisplayedInRM);

                    if (royalMailPO.lblAddressLine2.isDisplayed()) {
                        updateAddrLine2(addressLine2AsDisplayedInRM);

                        if (royalMailPO.lblAddressLine3.isDisplayed()) {
                            updateAddrLine3(addressLine3AsDisplayedInRM);
                            if (royalMailPO.lblAddressLine4.isDisplayed()) {
                                updateAddrLine4(addressLine4AsDisplayedInRM);
                            }
                        }
                    }
                    refcode = postCodeAsDisplayedInRM;
                    alreadyMatched = true;

                } else {
                    royalMailPO.txtPostCode.clear();
                    /*seld.driver.manage().deleteAllCookies();
                    seld.driver.get(PropertyFileHandler.properties.getProperty("url"));
                    uiButton.clickButton(royalMailPO.btnConsentSubmit);*/

                }
            }

            if (alreadyMatched != true) {
                for (int splitIter = 0; splitIter < prmRetailerAddress.getRetailerNameFragments().length; splitIter++) {

                    if (!Arrays.asList("and", "the", "store", "stores", "shop", "ltd").contains(prmRetailerAddress.getRetailerNameFragments()[splitIter]) && prmRetailerAddress.getRetailerNameFragments()[splitIter].contains(prmRetailerAddress.getStreet()) == false && (prmRetailerAddress.getRetailerNameFragments()[splitIter].contains(prmRetailerAddress.getDistrict()) == false || prmRetailerAddress.getDistrict() == "") && prmRetailerAddress.getRetailerNameFragments()[splitIter].contains(prmRetailerAddress.getCity()) == false && retailerNameWithoutApostrophe.contains(prmRetailerAddress.getRetailerNameFragments()[splitIter])) {
                        if (!(postCodeAsDisplayedInRM.equals(refcode))) {
                            updateCompanyName(retailerNameAsDisplayedInRM);
                            updateAddrLine1(addressLine1AsDisplayedInRM);
                            updateCity(cityAsDisplayedInRM);
                            updatePostalCode(postCodeAsDisplayedInRM);

                            if (royalMailPO.lblAddressLine2.isDisplayed()) {
                                updateAddrLine2(addressLine2AsDisplayedInRM);
                                if (royalMailPO.lblAddressLine3.isDisplayed()) {
                                    updateAddrLine3(addressLine3AsDisplayedInRM);
                                    if (royalMailPO.lblAddressLine4.isDisplayed()) {
                                        updateAddrLine4(addressLine4AsDisplayedInRM);
                                    }
                                }
                            }
                            refcode = postCodeAsDisplayedInRM;
                            alreadyMatched = true;
                            break;

                        } else {
                            royalMailPO.txtPostCode.clear();
                          /*  seld.driver.manage().deleteAllCookies();
                            seld.driver.get(PropertyFileHandler.properties.getProperty("url"));
                            uiButton.clickButton(royalMailPO.btnConsentSubmit);*/
                            break;

                        }

                    }
                }
            }


            if (alreadyMatched != true) {
                updateCommentOnName("Retailer name does not match");
            }

        } catch (Exception filoException) {
            filoException.printStackTrace();
        }

    }

    private void updateCompanyName(String prmRetailerName) {
        updateRetailerDataInExcel("NewRetailerName", prmRetailerName);
    }

    private void updateAddrLine1(String prmAddLine1) {
        updateRetailerDataInExcel("AddressLine1", prmAddLine1);
    }

    private void updateAddrLine2(String prmAddLine2) {
        updateRetailerDataInExcel("AddressLine2", prmAddLine2);
    }

    private void updateAddrLine3(String prmAddLine3) {
        updateRetailerDataInExcel("AddressLine3", prmAddLine3);
    }

    private void updateAddrLine4(String prmAddLine4) {
        updateRetailerDataInExcel("AddressLine4", prmAddLine4);
    }

    private void updateCity(String prmCity) {
        updateRetailerDataInExcel("NewCity", prmCity);
    }

    private void updatePostalCode(String prmPostCode) {
        updateRetailerDataInExcel("NewPostcode", prmPostCode);
    }

    private void updateCommentOnStreet(String prmComment) {
        updateRetailerDataInExcel("CommentOnStreet", prmComment);
    }

    private void updateCommentOnName(String prmComment) {
        updateRetailerDataInExcel("CommentOnName", prmComment);
    }

    private void updateRetailerDataInExcel(String prmToBeUpdateColumnName, String prmToBeUpdateColumnValue) {
        try {
            String excelWorkBookName = SerenityScenario.configProp.getProperty("addressInputExcelWorkBook");
            String excelDataSheetName = SerenityScenario.configProp.getProperty("addressInputDataSheet");

            Fillo fillo = new Fillo();
            Connection excelConnection = fillo.getConnection(excelWorkBookName);

            String strQuery = "update " + excelDataSheetName + " set " + prmToBeUpdateColumnName + " = '" + prmToBeUpdateColumnValue + "' where ROWNO = '" + RetailerAddress.getDataRowNo() + "'";
            System.out.println(strQuery);
            excelConnection.executeUpdate(strQuery);
        } catch (Exception Ex) {
            Ex.printStackTrace();
        }
    }
}