package et.adey.mfi.mobilemoney;

/**
 * Which wallet the client pays from. In Ethiopia the network decides the wallet: telebirr is
 * Ethio Telecom (numbers 09...), M-Pesa is Safaricom Ethiopia (numbers 07...).
 */
public enum PaymentMethod {
    TELEBIRR("09"),
    MPESA("07");

    private final String phonePrefix;

    PaymentMethod(String phonePrefix) {
        this.phonePrefix = phonePrefix;
    }

    public boolean acceptsPhone(String phone) {
        return phone != null && phone.startsWith(phonePrefix);
    }

    public String phonePrefix() {
        return phonePrefix;
    }
}
