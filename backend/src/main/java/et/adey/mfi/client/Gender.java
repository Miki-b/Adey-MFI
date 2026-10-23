package et.adey.mfi.client;

/** Our gender values. Fineract stores gender as a "code value" (an id) of its "Gender" code. */
public enum Gender {
    MALE("Male"),
    FEMALE("Female");

    private final String fineractName;

    Gender(String fineractName) {
        this.fineractName = fineractName;
    }

    public String fineractName() {
        return fineractName;
    }
}
