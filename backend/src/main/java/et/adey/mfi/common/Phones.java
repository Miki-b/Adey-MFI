package et.adey.mfi.common;

/** Phone numbers are personal data: logs only ever show a masked form. */
public final class Phones {

    private Phones() {
    }

    /** "0900123456" -> "0900****56", "251900123456" -> "251900****56". Short or empty values -> "****". */
    public static String mask(String phone) {
        if (phone == null || phone.length() < 8) {
            return "****";
        }
        return phone.substring(0, phone.length() - 6) + "****" + phone.substring(phone.length() - 2);
    }
}
