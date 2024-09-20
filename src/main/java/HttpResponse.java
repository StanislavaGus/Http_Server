import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class HttpResponse {
    private static final String CONTENT_LENGTH = "Content-Length";
    private static final String CONTENT_TYPE = "Content-Type";
    private static final String DEFAULT_CONTENT_TYPE = "text/plain";
    private static final String HTTP_VERSION = "HTTP/1.1";


    private final Status status;
    private final Map<String, String> fields;
    private final ByteBuffer body;

    public HttpResponse(Status status, Map<String, String> fields, ByteBuffer body) {
        this.status = status;
        this.fields = fields;
        this.body = body;
    }

    private void writeHead(PrintWriter printWriter){
        fields.put(CONTENT_LENGTH, String.valueOf(body.remaining()));
        //remaining - returns the number of elements between the current position and the limit

        fields.putIfAbsent(CONTENT_TYPE, DEFAULT_CONTENT_TYPE);
                //add key-value if key not in map

        printWriter.printf("%s %d %s\r\n", HTTP_VERSION, status.getStatusCode(), status.getStatusText());

        for(var entry : fields.entrySet()){
            printWriter.printf("%s: %s\r\n", entry.getKey(), entry.getValue());
        }

        printWriter.print("\r\n");
    }

    public void write(WritableByteChannel writableByteChannel) throws IOException {
        StringWriter head = new StringWriter();
        try(PrintWriter printWriter = new PrintWriter(head)){
            writeHead(printWriter);
        }
        writableByteChannel.write(ByteBuffer.wrap(head.toString().getBytes(StandardCharsets.UTF_8)));
        while (body.hasRemaining()){
            writableByteChannel.write(body);
        }
    }

    public static HttpResponse of(Status status, String body){
        ByteBuffer buffer = ByteBuffer.wrap(body.getBytes(StandardCharsets.UTF_8));
        return new HttpResponse(status, new HashMap<>(), buffer);
    }
}
