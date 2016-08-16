package my.test.exante.runners;

import my.test.exante.data.CandlestickData;
import my.test.exante.services.CandlestickDataAggregatorService;
import my.test.exante.data.TradingData;
import my.test.exante.services.TradingDataReaderService;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.util.concurrent.LinkedBlockingQueue;

public class CandlestickServiceRunner {

    public static void main(String[] args) throws IOException {
        SocketChannel tradingDataChannel = SocketChannel.open(new InetSocketAddress("127.0.0.1", 5555));
        tradingDataChannel.configureBlocking(false);

        LinkedBlockingQueue<TradingData> tradingDataQueue = new LinkedBlockingQueue<TradingData>(1000);
        final LinkedBlockingQueue<CandlestickData> candlestickDataQueue = new LinkedBlockingQueue<CandlestickData>(1000);

        TradingDataReceiverRunner receiver = new TradingDataReceiverRunner();
        receiver.setTradingDataSocketChannel(tradingDataChannel);
        receiver.setTradingDataReaderService(new TradingDataReaderService());
        receiver.setTradingDataQueue(tradingDataQueue);

        CandlestickDataAggregatorRunner aggregator = new CandlestickDataAggregatorRunner();
        aggregator.setCandlestickDataAggregatorService(new CandlestickDataAggregatorService());
        aggregator.setTradingDataQueue(tradingDataQueue);
        aggregator.setCandlestickDataQueue(candlestickDataQueue);

        Runnable candlestickConsumer = new Runnable() {
            @Override
            public void run() {
                System.out.println("consumer started");
                while (!Thread.currentThread().isInterrupted()) {
                    CandlestickData candlestickData = candlestickDataQueue.poll();
                    if (candlestickData != null) {
                        System.out.println("consumer read");
                        System.out.println(candlestickData.getJson());
                    }
                }
            }
        };
        Thread candlestickConsumerThread = new Thread(candlestickConsumer);
        candlestickConsumerThread.start();

        Thread receiverThread = new Thread(receiver);
        receiverThread.start();

        Thread aggregatorThread = new Thread(aggregator);
        aggregatorThread.start();

    }
}
