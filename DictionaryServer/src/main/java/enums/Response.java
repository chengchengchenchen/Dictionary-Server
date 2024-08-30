package enums;

public enum Response {
    // Success
    SUCCESS(200, "OK query"),
    ADDED(201, "Word added successfully"),
    UPDATED(202, "Word already updated"),
    REMOVED(204, "Word removed successfully"),


    // 4xx Server Error
    DUPLICATE(400, "Word duplicates"),
    NO_WORD(401, "Word is nonexistent"),
    NO_MEANING(402, "Meaning is nonexistent"),
    DUPLICATE_MEANING(403, "Meaning duplicates"),
    NO_ACTION(404, "Unknown action"),
    FORBIDDEN(405, "Invalid format"),

    // 5xx Client Error
    INTERNAL_SERVER_ERROR(500, "Internal server.Server Error"),
    NOT_IMPLEMENTED(501, "Not Implemented"),
    BAD_GATEWAY(502, "Bad Gateway"),
    SERVICE_UNAVAILABLE(503, "Service Unavailable");

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

    public static Response fromCode(int code) {
        for (Response response : Response.values()) {
            if (response.getCode() == code) {
                return response;
            }
        }
        throw new IllegalArgumentException("Unknown response code: " + code);
    }
}

