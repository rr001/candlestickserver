package my.test.exante.services;

import my.test.exante.data.CandlestickData;
import my.test.exante.util.RunnerBase;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Канал отправки свечей клиенту.
 * Готовые свечи выбираются из очереди @see CandlestickDistributionChannel#candlestickDistributionQueue
 */
public class CandlestickDistributionChannel extends RunnerBase {
    private static Logger log = LogManager.getLogger(CandlestickDistributionChannel.class);

    private static AtomicInteger COUNTER = new AtomicInteger(0);

    public static final int MAX_CANDLESTICK_MESSAGE_LENGTH = 200;

    private ByteBuffer outputBuffer = ByteBuffer.allocate(MAX_CANDLESTICK_MESSAGE_LENGTH);

    private ConcurrentLinkedQueue<CandlestickData> candlestickDistributionQueue = new ConcurrentLinkedQueue<CandlestickData>();

    private SocketChannel distributionSocketChannel;

    private int index = COUNTER.incrementAndGet();

    public CandlestickDistributionChannel(SocketChannel distributionSocketChannel) {
        this.distributionSocketChannel = distributionSocketChannel;
    }

    public ByteBuffer createMessage(CandlestickData candlestickData) throws UnsupportedEncodingException {
        outputBuffer.limit(outputBuffer.capacity());
        outputBuffer.position(0);
        outputBuffer.put(candlestickData.getJson().getBytes("ascii"));
        outputBuffer.put("\n".getBytes("ascii"));
        outputBuffer.limit(outputBuffer.position());
        outputBuffer.position(0);
        return outputBuffer;
    }

    @Override
    protected void execute() throws Exception {
        if (log.isTraceEnabled()) log.trace("enter");

        CandlestickData candlestickData = null;
        while (isRunning() && (candlestickData = candlestickDistributionQueue.poll()) != null) {
            if (log.isInfoEnabled()) log.info(String.format("Distributing candlestick %s to client channel %s", candlestickData, this));

            ByteBuffer buffer = createMessage(candlestickData);

            distributionSocketChannel.write(buffer);
        }

        if (log.isTraceEnabled()) log.trace("leave");
    }

    public ConcurrentLinkedQueue<CandlestickData> getCandlestickDistributionQueue() {
        return candlestickDistributionQueue;
    }

    public SocketChannel getDistributionSocketChannel() {
        return distributionSocketChannel;
    }

    @Override
    public String toString() {
        return "CandlestickDistributionChannel{" +
                "index=" + index +
                '}';
    }

    public void close() {
        log.trace("Closing socket channel");
        try {
            distributionSocketChannel.close();
        } catch (IOException e) {
            log.error("Error closing socket channel.", e);
        }
    }

}
