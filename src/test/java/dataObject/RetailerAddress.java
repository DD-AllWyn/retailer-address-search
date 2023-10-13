package dataObject;

import com.codoid.products.exception.FilloException;
import com.codoid.products.fillo.Recordset;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RetailerAddress {
    private static int rowNo = 0;
    private String retailerName = "";

    private String[] retailerNameFragments;
    private String framedRetailerName;
    private String street = "";
    private String city = "";
    private String[] cityFragments;
    private String district = "";
    private String postCode = "";
    private String[] postCodeFragments;
    private String streetNo1 = "";
    private String streetNo2 = "";

    private String alternateRetailerName1 = "";
    private String alternateRetailerName2 = "";
    private String tempRetailerName = "";

    public RetailerAddress(int prmRowNo, Recordset recordset) {
        try {
            //PROCESS THE ADDRESS DATA RECEIVED VIA RECORD SET
            recordset.next();
            rowNo = prmRowNo;
            //PROCESS RETAILER NAME
            retailerName = recordset.getField("RetailerName").toLowerCase().replaceAll("\\*+", "").replaceAll("\\.", "").replaceAll("  ", " ").trim();
            processRetailerName(retailerName);

            //PROCESS STREET DETAILS
            street = recordset.getField("RetailerName").toLowerCase().replaceAll("/", "-").trim();
            processStreetDetails(street);

            //PROCESS DISTRICT DETAILS
            district = recordset.getField("District");
            processDistrictDetails(district);

            //PROCESS CITY DETAILS
            city = recordset.getField("City");
            processCity(city);

            //PROCESS POSTCODE DETAILS
            postCode = recordset.getField("Postcode");
            processPostCode(postCode);

        } catch (FilloException filloException) {
            filloException.printStackTrace();
        }
    }

    private void processRetailerName(String prmRetailerName) {
        String processedRetailerName = prmRetailerName.replaceAll("/|\\(|\\)", "").replaceAll("\\*", "").replaceAll("'", " ");
        retailerNameFragments = processedRetailerName.split("\\s+|-");


        String retailerNameFirstPart = retailerNameFragments[0];
        if (retailerNameFirstPart.length() == 1) {
            framedRetailerName = retailerNameFirstPart + " " + getRetailerNameFragments()[1];
        } else if (retailerNameFirstPart.equals("the")) {
            framedRetailerName = retailerNameFragments[1];
        } else {
            framedRetailerName = retailerNameFragments[0];
        }

        //SET THE ALTERNATE NAMES FOR THE RETAILER
        String[] ambiguousRetailerNames = new String[]{"po", "co-op", "co -op", "co op", "morrisons", "spso", "p o"};
        for (int namesIter = 0; namesIter < ambiguousRetailerNames.length; namesIter++) {
            if (retailerName.contains(ambiguousRetailerNames[namesIter])) {

                switch (namesIter) {
                    case 0:
                    case 5:
                    case 6:
                        alternateRetailerName1 = "post office";
                        alternateRetailerName2 = "post office";
                        tempRetailerName = ambiguousRetailerNames[namesIter];
                        break;
                    case 1:
                    case 2:
                    case 3:
                        alternateRetailerName1 = "co-operative";
                        alternateRetailerName2 = "co operative";
                        tempRetailerName = ambiguousRetailerNames[namesIter];
                        break;
                    case 4:
                        alternateRetailerName1 = "morrison";
                        alternateRetailerName2 = "morrison";
                        tempRetailerName = ambiguousRetailerNames[namesIter];
                        break;
                    default:
                        alternateRetailerName1 = "";
                        alternateRetailerName2 = "";
                        tempRetailerName = "someunknownjunkvalue";
                }
                break;
            }
        }
    }


    private void processStreetDetails(String prmStreet) {
        //EXTRACT STREET NUMBER DETAILS
        Pattern pattern = Pattern.compile("^\\d+-?/?(\\d+)?");
        Matcher matcher = pattern.matcher(prmStreet);
        String streetNumberInString = "";
        streetNo1 = "";
        streetNo2 = "";
        while (matcher.find()) {
            streetNumberInString = matcher.group();
            String[] splitStreetNumberInString = streetNumberInString.split("-");
            if (splitStreetNumberInString.length == 1) {
                streetNo1 = splitStreetNumberInString[0];
                System.out.println("Street number one is :" + streetNo1);
            } else {
                streetNo1 = splitStreetNumberInString[0];
                streetNo2 = splitStreetNumberInString[1];
                System.out.println("Street number one is :" + streetNo1);
                System.out.println("Street number two is :" + streetNo2);
            }
        }
    }

    private void processDistrictDetails(String prmDistrict) {
        district = prmDistrict == "" ? "" : prmDistrict.toLowerCase().replaceAll(",", "").trim();
    }

    private void processCity(String prmCity) {
        cityFragments = prmCity.split("-");
    }


    public void processPostCode(String prmPostCode) {
        postCodeFragments = postCode.split(" ");
    }

    public static int getDataRowNo() {
        return rowNo;
    }

    public String getRetailerName() {
        return retailerName;
    }

    public String[] getRetailerNameFragments() {
        return retailerNameFragments;
    }

    public String getFramedRetailerName() {
        return framedRetailerName;
    }

    public String getAlternateRetailerName1() {
        return alternateRetailerName1;
    }

    public String getAlternateRetailerName2() {
        return alternateRetailerName2;
    }

    public String getTempRetailerName() {
        return tempRetailerName;
    }


    public String getStreet() {
        return street;
    }

    public String getModifiedStreet() {
        return street.replaceAll("^\\d+-?/?(\\d+)?", "").trim();
    }

    public String getStreetNo1() {
        return streetNo1;
    }

    public String getStreetNo2() {
        return streetNo2;
    }

    public String getCity() {
        return city;
    }

    public String[] getCityFragments() {
        return cityFragments;
    }

    public String getDistrict() {

        return district;
    }

    public String getPostCode() {
        return postCode;
    }

    public String[] getPostCodeFragments() {
        return postCodeFragments;
    }
}
