package et.adey.mfi.common;

/** Fineract staff display names are "Lastname, Firstname " -> we show "Firstname Lastname". */
public final class Names {

    private Names() {
    }

    public static String readable(String fineractDisplayName) {
        if (fineractDisplayName == null) {
            return null;
        }
        String name = fineractDisplayName.trim();
        int comma = name.indexOf(',');
        return comma < 0 ? name : (name.substring(comma + 1).trim() + " " + name.substring(0, comma).trim());
    }
}
