public enum Methods {
    GET,
    PUT,
    POST,
    PATCH,
    DELETE;

    public static Methods convertFromStringToEnum(String string){
        return switch(string){
            case "GET" -> GET;
            case "POST" -> POST;
            case "PUT" -> PUT;
            case "PATCH" -> PATCH;
            case "DELETE" -> DELETE;
            default -> throw new IllegalArgumentException("HTTP method " + string + " is not implemented");

        };
    }
}
