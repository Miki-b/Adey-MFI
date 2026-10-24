package et.adey.mfi.mobilemoney;

/** Lets tests in other packages set up intents (the setters are package-private on purpose). */
public final class PaymentIntentTestAccess {

    private PaymentIntentTestAccess() {
    }

    public static void chargeCreated(PaymentIntent intent, String chargeId) {
        intent.chargeCreated(chargeId);
    }
}
