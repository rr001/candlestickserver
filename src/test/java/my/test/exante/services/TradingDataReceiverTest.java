package my.test.exante.services;

import junit.framework.Assert;
import my.test.exante.data.CandlestickDataTest;
import my.test.exante.TestUtil;
import my.test.exante.data.CandlestickData;
import my.test.exante.data.TradingData;
import my.test.exante.util.CurrentTimeService;
import my.test.exante.util.CurrentTimeServiceImpl;
import org.junit.Test;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ConcurrentLinkedQueue;

public class TradingDataReceiverTest {

    SocketChannel createSocketChannel() throws IOException {
        TradingData[] datas = new TradingData[]{CandlestickDataTest.data11,
                CandlestickDataTest.data12,
                CandlestickDataTest.data13,
                CandlestickDataTest.data14,
                CandlestickDataTest.data5
        };

        SocketChannel sc = TestUtil.createTradingDataSocketChannel(datas);
        return sc;
    }

    @Test
    public void testRun() throws InterruptedException, IOException {
        CurrentTimeService currentTimeService = new CurrentTimeServiceImpl();
//        CandlestickDataAggregatorService aggregator = new CandlestickDataAggregatorService();
//        aggregator.setCurrentTimeService(currentTimeService);
//        TradingDataReceiver receiver = new TradingDataReceiver();
//        receiver.setTradingDataSocketChannel(createSocketChannel());
//        receiver.setCandlestickDataAggregatorService(aggregator);
//        receiver.setTradingDataReaderService(new TradingDataReaderService());
//        ConcurrentLinkedQueue<CandlestickData> candlestickDataQueue = new ConcurrentLinkedQueue<CandlestickData>();
//        receiver.setCandlestickOutputQueue(candlestickDataQueue);
//
//        Thread receiverThread = new Thread(receiver);
//        receiverThread.start();
//        Thread.sleep(2000);
//        receiver.stop();
//        receiverThread.join(2000);
//        Assert.assertTrue("Receiver shoud be already stopped", receiver.isIdle());
//
//        CandlestickData data1 = candlestickDataQueue.poll();
//        Assert.assertNotNull("First candlestick", data1);
//        CandlestickData data2 = candlestickDataQueue.poll();
//        Assert.assertNotNull("Second candlestick", data2);
//

    }
}
