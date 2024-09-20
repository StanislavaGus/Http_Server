import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public class Main {
    public static final int PORT = 8081;
    private static final String NAME_PARAM = "name=";

    private static class PersonHandler implements HttpHandler {
        private static final Map<String, String> persons = new HashMap<>();

        @Override
        public HttpResponse handle(HttpRequest request) {
            try {
                String name = extractNameFromQuery(request.getTarget().getQuery());

                if (Methods.PUT.equals(request.getMethod())) {
                    String previousPerson = persons.put(name, request.getBody());
                    return HttpResponse.of(previousPerson == null ? Status.Created : Status.Ok, "");
                }
                if (Methods.GET.equals(request.getMethod())) {
                    String data = persons.get(name);
                    return data == null ? HttpResponse.of(Status.NotFound, "") : HttpResponse.of(Status.Ok, data);
                }
                return HttpResponse.of(Status.BadRequest, "Unsupported HTTP method");
            } catch (IllegalArgumentException e) {
                System.out.println("Invalid request: " + e.getMessage());
                return HttpResponse.of(Status.BadRequest, "Invalid request: " + e.getMessage());
            }
        }

        private String extractNameFromQuery(String query) {
            return Optional.ofNullable(query)
                    .flatMap(q -> Arrays.stream(q.split("&"))
                            .filter(param -> param.startsWith(NAME_PARAM))
                            .findFirst()
                    )
                    .map(param -> param.substring(NAME_PARAM.length()))
                    .orElseThrow(() -> new IllegalArgumentException("Missing 'name' parameter"));
        }
    }

    public static void main(String[] args) throws IOException, URISyntaxException {
        Router router = new Router()
                .addDefaultHandler(Main::handleNotFound)
                .addRouter(new URI("/"), EnumSet.of(Methods.GET), Main::handleRoot)
                .addRouter(new URI("/person"), EnumSet.of(Methods.GET, Methods.PUT), new PersonHandler());

        CompletableFuture<Void> serverFuture = CompletableFuture.runAsync(() -> {
            try (HttpServer server = new HttpServer(PORT, router)) {
                server.start();
                System.out.println("Starting server on port " + PORT);
            } catch (IOException e) {
                System.out.println("Error while starting the server");
            }
        });

        handleServerControl(serverFuture);
    }

    private static HttpResponse handleNotFound(HttpRequest request) {
        System.out.println("404 Not Found: " + request.getTarget());
        return HttpResponse.of(Status.NotFound, "Not Found");
    }

    private static HttpResponse handleRoot(HttpRequest request) {
        StringBuilder sb = new StringBuilder("Your headers are:\n");
        request.headers.forEach((key, value) -> sb.append(key).append(":\t").append(value).append('\n'));
        return HttpResponse.of(Status.Ok, sb.toString());
    }

    private static void handleServerControl(CompletableFuture<Void> serverFuture) {
        try (Scanner scanner = new Scanner(System.in)) {
            String input;
            do {
                input = scanner.nextLine();
            } while (!"stop".equalsIgnoreCase(input));

            System.out.println("Stopping server...");
            serverFuture.cancel(true);
        } catch (Exception e) {
            System.out.println("Error while handling server control");
        }
    }
}
