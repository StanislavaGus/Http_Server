import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.*;
import java.nio.channels.spi.SelectorProvider;

public class HttpServer implements Server {

    private final InetSocketAddress address;
    private final Router router;
    private ServerSocketChannel server;
    private volatile boolean isRunning = false;

    public HttpServer(int port, Router router) {
        this(new InetSocketAddress(port), router);
    }

    public HttpServer(InetSocketAddress address, Router router) {
        this.router = router;
        this.address = address;
    }

    @Override
    public void start() throws IOException {
        Selector selector = SelectorProvider.provider().openSelector();
        server = ServerSocketChannel.open();
        server.bind(address);
        server.configureBlocking(false);
        server.register(selector, SelectionKey.OP_ACCEPT);

        isRunning = true;
        System.out.println("Server started at " + address);

        while (isRunning) {
            selector.select(key -> {
                if (!key.isValid()) {
                    return;
                }

                try {
                    if (key.isAcceptable()) {
                        handleAccept(key, selector);
                    } else if (key.isReadable() && key.isWritable()) {
                        handleReadWrite(key);
                    }
                } catch (IOException e) {
                    System.err.println("Error handling key: " + e.getMessage());
                    key.cancel();
                    closeChannel(key.channel());
                }
            });
        }
    }

    private void handleAccept(SelectionKey key, Selector selector) throws IOException {
        ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
        SocketChannel clientChannel = serverChannel.accept();
        clientChannel.configureBlocking(false);
        clientChannel.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
        System.out.println("Accepted connection from " + clientChannel.getRemoteAddress());
    }

    private void handleReadWrite(SelectionKey key) throws IOException {
        SocketChannel clientChannel = (SocketChannel) key.channel();
        try {
            HttpRequest request = new HttpRequest(clientChannel);
            request.parse();

            HttpResponse response = router.getHandler(request).handle(request);
            response.write(clientChannel);
        } catch (HttpRequest.BadRequestException e) {
            System.err.println("Bad request: " + e.getMessage());
            // Можно отправить клиенту HTTP-ответ с кодом ошибки
        } finally {
            clientChannel.close();
        }
    }

    private void closeChannel(Channel channel) {
        try {
            if (channel != null && channel.isOpen()) {
                channel.close();
            }
        } catch (IOException e) {
            System.err.println("Failed to close channel: " + e.getMessage());
        }
    }

    @Override
    public void close() throws IOException {
        isRunning = false;
        if (server != null && server.isOpen()) {
            server.close();
        }
        System.out.println("Server stopped");
    }
}
