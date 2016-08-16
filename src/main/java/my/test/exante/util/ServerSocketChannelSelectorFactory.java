package my.test.exante.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;

/**
 * Вспомогательный класс для создания серверных сокетов
 */
public class ServerSocketChannelSelectorFactory {
    private static Logger log = LogManager.getLogger(ServerSocketChannelSelectorFactory.class);

    private InetSocketAddress listeningAddress;

    private Selector selector;
    private ServerSocketChannel serverSocketChannel;

    public void open() throws IOException {
        selector = Selector.open();
        serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.bind(listeningAddress);
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
    }

    public void close() {
        try {
            serverSocketChannel.close();
        } catch (Exception e) {
            log.error(e);
        }

        try {
            selector.close();
        } catch (Exception e) {
            log.error(e);
        }
    }

    public Selector getSelector() {
        return selector;
    }

    public ServerSocketChannel getServerSocketChannel() {
        return serverSocketChannel;
    }

    public InetSocketAddress getListeningAddress() {
        return listeningAddress;
    }

    public void setListeningAddress(InetSocketAddress listeningAddress) {
        this.listeningAddress = listeningAddress;
    }
}
