/*
Student Name: Jingcheng Qian
Student ID: 1640690
*/

package enums;

public enum Request {
    // CRUD Operations
    CREATE("CREATE", "Create a new word"),
    ADD("ADD", "Add a new word meaning"),
    SEARCH("SEARCH", "Search a word"),
    UPDATE("UPDATE", "Update an existing word"),
    REMOVE("REMOVE", "Remove an existing word");

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
}
