import java.net.URI;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Router {
    private final Map<String, EnumMap<Methods, HttpHandler>> routeMap = new TreeMap<>();
    private final EnumMap<Methods, HttpHandler> defaultHandlers = new EnumMap<>(Methods.class);

    public Router addRouter(URI uri, EnumSet<Methods> methods, HttpHandler handler){
        String path = uri.normalize().getPath();

        EnumMap<Methods, HttpHandler> methodHandlerMap = methods.stream()
                .collect(Collectors.toMap(
                        Function.identity(),
                        method -> handler,
                        (existing, replacement) -> replacement,
                        () -> new EnumMap<>(Methods.class)
                ));

        routeMap.merge(path, methodHandlerMap, (existingMap, newMap) -> {
            existingMap.putAll(newMap);
            return existingMap;
        });

        return this;
    }

    public Router addDefaultHandler(EnumSet<Methods> methods, HttpHandler handler) {
        methods.forEach(method -> defaultHandlers.put(method, handler));
        return this;
    }

    public Router addDefaultHandler(HttpHandler handler) {
        return addDefaultHandler(EnumSet.allOf(Methods.class), handler);
    }


    public HttpHandler getHandler(URI uri, Methods method){
        String normalizedPath = normalizePath(uri);
        HttpHandler handler = routeMap.getOrDefault(normalizedPath, defaultHandlers)
                .getOrDefault(method, defaultHandlers.get(method));

        if (handler == null) {
            throw new RouteNotFoundException("No handler found for " + method + " " + normalizedPath);
        }

        return handler;
    }

    public HttpHandler getHandler (HttpRequest request){
        return getHandler(request.getTarget(), request.getMethod());
    }

    private String normalizePath (URI uri){
        return uri.normalize().getPath();
    }

    // Кастомное исключение для отсутствия маршрута
    public static class RouteNotFoundException extends NoSuchElementException {
        public RouteNotFoundException(String message) {
            super(message);
        }
    }
}