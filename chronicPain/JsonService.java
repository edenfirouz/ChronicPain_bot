package chronicPain;

import chronicPain.util.MedInfo;

/**
 * This class contains functions that deal with String of the json.
 */
public class JsonService {


    /**
     * @param message - this param is the recommendation of the treatment that we got from Picard
     * @return - string of json object after building.
     */
    public static String buildRecommendation(String message) {
        String[] recommendationFields = message.split("\\*");
        if (recommendationFields.length == 1)
            return message;
        String and = "";

        // format: treat.medicineName + "*" + treat.baseDosage + "*" + treat.unit + "*" + treat.amount + "*" + treat.tabsOrUnits

        if (recommendationFields[MedInfo.TABS_OR_UNITS].contains("לרופא")) {
            and = "ופנה לרופא";
        } else
            and = "והמתן שעה";

        // Checks if the recommendation has the right number of fields.
        if (recommendationFields.length > 4) {
            String displayMessage = "";

            // If the tabsOrUnits field contains nothing means we need to print an empty line.
            if (recommendationFields[MedInfo.TABS_OR_UNITS] == "" || recommendationFields[MedInfo.TABS_OR_UNITS] == " ") {
                recommendationFields[MedInfo.AMOUNT] = "";
                recommendationFields[MedInfo.TABS_OR_UNITS] = "\n";
            }


            if (recommendationFields[MedInfo.TABS_OR_UNITS].contains("Tabs")) {
                recommendationFields[MedInfo.TABS_OR_UNITS] = "כדורים";
                if (recommendationFields[MedInfo.AMOUNT].equals("1")) {
                    recommendationFields[MedInfo.TABS_OR_UNITS] = "כדור";
                }
            }

            if (recommendationFields[MedInfo.TABS_OR_UNITS].contains("Sprays")) {
                recommendationFields[MedInfo.TABS_OR_UNITS] = "מנות";
                if (recommendationFields[MedInfo.AMOUNT].equals("1")) {
                    recommendationFields[MedInfo.TABS_OR_UNITS] = "מנה";
                }
            }

            if (recommendationFields[MedInfo.TABS_OR_UNITS].contains("ml")) {
                recommendationFields[MedInfo.TABS_OR_UNITS] = "סמ\"ק";
                recommendationFields[MedInfo.UNIT] = "";
                recommendationFields[MedInfo.BASE_DOSAGE] = "";
            }

            if (recommendationFields[MedInfo.TABS_OR_UNITS].contains("mg")) {
                recommendationFields[MedInfo.TABS_OR_UNITS] = "מ\"ג";
                recommendationFields[MedInfo.UNIT] = "";
                recommendationFields[MedInfo.BASE_DOSAGE] = "";
            }

            if (recommendationFields[MedInfo.MEDICINE_NAME].equals("ABSTRAL")) {
                recommendationFields[MedInfo.TABS_OR_UNITS] = "טבליות";
                if (recommendationFields[MedInfo.AMOUNT].equals("1")) {
                    recommendationFields[MedInfo.TABS_OR_UNITS] = "טבליה";
                }
                displayMessage += "נא לשים מתחת ללשון ";
            } else if (recommendationFields[MedInfo.MEDICINE_NAME].equals("PECFENT")) {
                displayMessage += "נא לרסס בנחיר ";
            } else displayMessage += "טול ";

            String temp = "";
            if (recommendationFields[MedInfo.TABS_OR_UNITS].equals("כדור") ||
                    recommendationFields[MedInfo.TABS_OR_UNITS].equals("טבליה") ||
                    recommendationFields[MedInfo.TABS_OR_UNITS].equals("מנה")) {
                if (recommendationFields[MedInfo.TABS_OR_UNITS].equals("כדור"))
                    temp = recommendationFields[MedInfo.TABS_OR_UNITS] + " " + "אחד";
                else
                    temp = recommendationFields[MedInfo.TABS_OR_UNITS] + " " + "אחת";
            } else {
                if (recommendationFields[MedInfo.TABS_OR_UNITS].contains("סמ\"ק") ||
                        recommendationFields[MedInfo.TABS_OR_UNITS].contains("מנות") ||
                        recommendationFields[MedInfo.TABS_OR_UNITS].contains("טבליות") ||
                        recommendationFields[MedInfo.TABS_OR_UNITS].contains("כדורים") ||
                        recommendationFields[MedInfo.TABS_OR_UNITS].contains("מ\"ג")) {
                    temp = recommendationFields[MedInfo.AMOUNT] + " " + recommendationFields[MedInfo.TABS_OR_UNITS];
                }
            }
            displayMessage += temp;
            if (!recommendationFields[MedInfo.MEDICINE_NAME].equals("Actiq")) {
                displayMessage += " " + "של";
            }
            displayMessage += " ";
            displayMessage += recommendationFields[MedInfo.MEDICINE_NAME] + " "
                    + recommendationFields[MedInfo.BASE_DOSAGE] + recommendationFields[MedInfo.UNIT];

            displayMessage += " " + and;
            return displayMessage;
        }
        return message.replaceAll("\\*", " ");
    }

    /**
     * @param json - string of json object with "OldValue".
     * @return - string of json object without "OldValue".
     */
    public static String clearOldValue(String json) {
        json = json.replaceAll("\"OldValue\":\"FALSE\",", "");
        json = json.replaceAll("\"OldValue\":\"TRUE\",", "");
        json = json.replaceAll("\"OldValue\":\"\",", "");

        for (int i = 0; i < 11; i++) {
            json = json.replaceAll("\"OldValue\":\"" + i + "\",", "");
        }

        return json;
    }
}