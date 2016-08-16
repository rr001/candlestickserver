package my.test.exante.runners;

import my.test.exante.services.CandlestickDataDistributorService;
import my.test.exante.util.RunnerBase;

import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

/**
 * Поток обработки входящих соединений от потребителей.
 */
public class CandlestickServerSocketListenerRunner extends RunnerBase {

    private Selector selector;

    /**
     * Сервис отправки свечей потребителям
     */
    private CandlestickDataDistributorService candlestickDataDistributorService;

    @Override
    protected void execute() throws Exception {

        while (isRunning()) {

            // слушаем серверный сокет
            int commActivityCount = selector.select();

            if (commActivityCount > 0) {
                Set<SelectionKey> selectionKeys = selector.selectedKeys();

                Iterator<SelectionKey> iter = selectionKeys.iterator();

                while (iter.hasNext()) {
                    SelectionKey selectionKey = iter.next();

                    iter.remove();
                    if (!selectionKey.isValid()) {
                        continue;
                    }
                    if (selectionKey.isAcceptable()) {
                        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) selectionKey.channel();
                        SocketChannel clientChannel = serverSocketChannel.accept();
                        if (clientChannel != null) {
                            clientChannel.configureBlocking(false);
                            clientChannel.register(selector, SelectionKey.OP_WRITE);
                            // создаем новый канал
                            candlestickDataDistributorService.createNewDistributionChannel(clientChannel);
                        }
                    }
                }

            }
        }
    }

    public Selector getSelector() {
        return selector;
    }

    public void setSelector(Selector selector) {
        this.selector = selector;
    }

    public CandlestickDataDistributorService getCandlestickDataDistributorService() {
        return candlestickDataDistributorService;
    }

    public void setCandlestickDataDistributorService(CandlestickDataDistributorService candlestickDataDistributorService) {
        this.candlestickDataDistributorService = candlestickDataDistributorService;
    }
}