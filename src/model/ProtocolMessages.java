package model;

public class ProtocolMessages {
    public static final String REGISTRATION_REQUEST = "REGISTRATION_REQUEST";
    public static final String LOGIN_REQUEST = "LOGIN_REQUEST";
    public static final String LIST_CONTACTS_REQUEST = "LIST_CONTACTS_REQUEST";

    public static final String USERNAME_AVAILABLE = "USERNAME_AVAILABLE";
    public static final String USERNAME_TAKEN = "USERNAME_TAKEN";
    public static final String LOGIN_SUCCESFUL = "LOGIN_SUCCESFUL";
    public static final String LOGIN_BAD = "LOGIN_BAD";

    public static final String CLIENT_CONNECTION_CLOSURE = "CLIENT_CONNECTION_CLOSURE";

    public static final String BROADCAST_MESSAGE = "BROADCAST_MESSAGE";
    public static final String PRIVATE_MESSAGE = "PRIVATE_MESSAGE";
    public static final String ERROR_MESSAGE = "ERROR_MESSAGE";
}
