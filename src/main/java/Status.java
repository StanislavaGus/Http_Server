public enum Status {
    Ok(200, "OK"),
    Created(201, "Created"),

    // 4xx Client Errors
    BadRequest(400, "Bad Request"),
    NotFound(404, "Not Found"),
    LengthRequired(411, "Length Required"),

    // 5xx Server Errors
    InternalServerError(500, "Internal Server Error"),
    NotImplemented(501, "Not Implemented"),
    HttpVersionNotSupported(505, "HTTP Version Not Supported");

    public int getStatusCode() {
        return statusCode;
    }

    public String getStatusText() {
        return statusText;
    }

    private final int statusCode;
    private final String statusText;

    Status(int i, String s) {
        statusCode = i;
        statusText = s;
    }


}
