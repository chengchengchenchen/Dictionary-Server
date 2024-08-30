package enums;

public enum Request {
    // CRUD Operations
    ADD("ADD", "Add a new word meaning"),
    SEARCH("SEARCH", "Search a word"),
    UPDATE("UPDATE", "Update an existing word"),
    REMOVE("REMOVE", "Remove an existing word"),

    // Additional Operations
    CONNECT("CONNECT", "Establish a connection"),
    DISCONNECT("DISCONNECT", "Terminate a connection");

    private final String action;
    private final String description;

    Request(String action, String description) {
        this.action = action;
        this.description = description;
    }

    public String getAction() {
        return action;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return action + ": " + description;
    }

    public static Request fromAction(String action) {
        for (Request request : Request.values()) {
            if (request.getAction().equalsIgnoreCase(action)) {
                return request;
            }
        }
        throw new IllegalArgumentException("Unknown request action: " + action);
    }
}
