package bogen.studio.cas_client.Enum;


public enum AuthVia {

    SMS, MAIL;

    public String getName() {
        return name().toLowerCase();
    }
}
