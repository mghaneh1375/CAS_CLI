package bogen.studio.cas_client.Utility;

public class StaticValues {

    public final static int SMS_RESEND_SEC = 300;
    public final static int SMS_RESEND_MSEC = 1000 * 60 * 5;
    public final static int SMS_VALIDATION_EXPIRATION_MSEC = 1000 * 60 * 10;


    public static final String[] VALID_DOMAINS = new String[]{
            "boom.bogenstudio.com",
            "passenger.bogenstudio.com",
            "business.bogenstudio.com",
            "koochita-server.bogenstudio.com",
            "panel.koochita.com",
    };

}
