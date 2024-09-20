import java.io.IOException;

public interface Server extends AutoCloseable {
    void start() throws IOException;
}
