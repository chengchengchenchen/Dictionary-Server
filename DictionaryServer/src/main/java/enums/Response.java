package enums;

public enum Response {
    // 2xx Success
    SUCCESS(200, "SUCCESS"),
    CREATED(201, "Created"),
    ACCEPTED(202, "Accepted"),
    NO_CONTENT(204, "No Content"),


    // 4xx Client Error
    BAD_REQUEST(400, "Bad enums.Request"),
    UNAUTHORIZED(401, "Unauthorized"),
    FORBIDDEN(403, "Forbidden"),
    NOT_FOUND(404, "Not Found"),

    // 5xx server.Server Error
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

