package enums;

public enum Response {
    // Success
    SUCCESS(200, "OK query"),
    CREATED(201, "Word created successfully"),
    ADDED(202, "Meaning added successfully"),
    UPDATED(203, "Word meaning already updated"),
    REMOVED(204, "Word removed successfully"),


    // 4xx Server Error
    DUPLICATE(400, "Word duplicates"),
    NO_WORD(401, "Word is nonexistent"),
    NO_MEANING(402, "Meaning is nonexistent"),
    DUPLICATE_MEANING(403, "Meaning duplicates"),
    NO_ACTION(404, "Unknown action"),
    FORBIDDEN(405, "Invalid format");

    private final int code;
    private final String description;

    Response(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return code + " " + description;
    }

}

