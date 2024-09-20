import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class HttpRequest {

    // ByteCharSequence для работы с ByteBuffer как с CharSequence
    static class ByteCharSequence implements CharSequence {
        private final ByteBuffer internal;

        private ByteCharSequence(ByteBuffer internal) {
            this.internal = internal;
        }

        @Override
        public int length() {
            return internal.remaining();
        }

        @Override
        public char charAt(int index) {
            return (char) internal.get(index);
        }

        @Override
        public CharSequence subSequence(int start, int end) {
            ByteBuffer slice = internal.slice();
            slice.position(start);
            slice.limit(end);
            return new ByteCharSequence(slice);
        }

        public static CharSequence of(ByteBuffer buffer) {
            return new ByteCharSequence(buffer);
        }
    }

    // Регулярные выражения для работы с HTTP
    private static final Pattern CRLF = Pattern.compile("\r\n");
    private static final Pattern CRLFCRLF = Pattern.compile("\r\n\r\n");
    private static final Pattern SP = Pattern.compile(" ");
    private static final Pattern VERSION = Pattern.compile("HTTP/1\\.\\d");
    private static final Pattern TOKEN = Pattern.compile("[!#$%&'*+-.^`|~\\w]+");
    private static final Pattern STRIP_REGEX = Pattern.compile("[ \t]*(.*)[ \t]*");
    private static final Pattern OBSOLETE_FOLD = Pattern.compile("^[ \t].*");

    // Размер буфера
    private static final int BUFFER_CAPACITY_BYTES = 8192;

    // Поля класса
    private final ReadableByteChannel byteChannel;
    private Methods method;
    private URI target;
    private byte[] body;
    public final Map<String, String> headers = new HashMap<>();

    // Конструктор
    public HttpRequest(ReadableByteChannel channel) {
        this.byteChannel = channel;
    }

    // Получить тело запроса в виде строки
    public String getBody() {
        return new String(body, StandardCharsets.UTF_8);
    }

    // Получить HTTP метод
    public Methods getMethod() {
        return method;
    }

    // Получить URI запроса
    public URI getTarget() {
        return target;
    }

    // Метод для разбора запроса
    public void parse() throws IOException, BadRequestException {
        ByteBuffer buf = ByteBuffer.allocate(BUFFER_CAPACITY_BYTES);
        int bytesRead;
        while (true) {
            bytesRead = byteChannel.read(buf);
            if (bytesRead == -1 && !buf.hasRemaining()) {
                throw new BadRequestException("No header separator found");
            }
            buf.flip(); // Переключаем в режим чтения
            Matcher matcher = CRLFCRLF.matcher(ByteCharSequence.of(buf));
            if (matcher.find()) {
                parseHeaders(buf, matcher.end());
                break;
            }
            buf.compact(); // Освобождаем место для новых данных
        }
        buf.clear();
        if (method == Methods.POST || method == Methods.PUT || method == Methods.PATCH) {
            body = readBody(buf);
        }
    }

    // Парсинг стартовой линии запроса
    private void parseRequestLine(String requestLine) throws BadRequestException {
        String[] parts = requestLine.split(" ");
        if (parts.length < 3) {
            throw new BadRequestException("Invalid request line");
        }
        method = Methods.convertFromStringToEnum(parts[0]);
        try {
            target = new URI(parts[1]);
        } catch (URISyntaxException e) {
            throw new BadRequestException("Invalid URI");
        }
        if (!VERSION.matcher(parts[2]).matches()) {
            throw new BadRequestException("Unsupported HTTP version");
        }
    }


    // Парсинг заголовков
    private void parseHeaders(ByteBuffer buf, int headerEnd) throws BadRequestException {
        buf.position(0);
        buf.limit(headerEnd);
        String headersString = StandardCharsets.UTF_8.decode(buf).toString();
        Scanner scanner = new Scanner(headersString);
        parseRequestLine(scanner.nextLine()); // Разбор первой линии
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            if (line.isEmpty()) {
                break;
            }
            var keyValue = line.split(":", 2);
            if (keyValue.length < 2) {
                throw new BadRequestException("Invalid header format");
            }
            headers.put(keyValue[0].trim(), keyValue[1].trim());
        }
    }

    // Чтение тела запроса
    private byte[] readBody(ByteBuffer buf) throws IOException, BadRequestException {
        String contentLengthValue = headers.get("Content-Length");
        if (contentLengthValue == null) {
            throw new BadRequestException("Content-Length header is required");
        }
        int contentLength = Integer.parseInt(contentLengthValue);
        byte[] bodyData = new byte[contentLength];

        // Чтение данных из буфера
        int bytesToReadFromBuffer = Math.min(contentLength, buf.remaining());
        buf.get(bodyData, 0, bytesToReadFromBuffer);

        int bytesRead = bytesToReadFromBuffer;

        // Если данные не все прочитаны, продолжаем читать с канала
        while (bytesRead < contentLength) {
            ByteBuffer tempBuf = ByteBuffer.wrap(bodyData, bytesRead, contentLength - bytesRead);
            int read = byteChannel.read(tempBuf);
            if (read == -1) {
                throw new BadRequestException("Unexpected end of stream");
            }
            bytesRead += read;
        }

        return bodyData;
    }


    // Исключение для плохого запроса
    public static class BadRequestException extends Exception {
        public BadRequestException(String message) {
            super(message);
        }
    }
}
