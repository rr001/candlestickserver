package my.test.exante.services;

import junit.framework.Assert;
import my.test.exante.CandlestickServerException;
import my.test.exante.TestUtil;
import my.test.exante.data.TradingData;
import org.junit.Test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;

public class TradingDataReaderServiceTest {
    @Test
    public void testSelfTest() throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(100);
        buffer.put("Test".getBytes());
        buffer.limit(buffer.position());
        buffer.position(0);
        SocketChannel channel = TestUtil.createSocketChannel(buffer);
        ByteBuffer buffer2 = ByteBuffer.allocate(100);
        int bytesRead = channel.read(buffer2);
        String s = new String(buffer2.array(), 0, bytesRead);
        Assert.assertEquals(bytesRead, buffer.limit());
        Assert.assertEquals(s, "Test");

        buffer2.position(0);
        bytesRead = channel.read(buffer2);
        Assert.assertEquals(0, bytesRead);

    }

    @Test
    public void testReceiveMessage() throws IOException, CandlestickServerException {
        TradingData[] datas = new TradingData[]{new TradingData("ABC", System.currentTimeMillis(), 123.4, 345),
                new TradingData("SDFGH", System.currentTimeMillis(), 345.6, 3342),
                new TradingData("A", System.currentTimeMillis(), 4512.3, 754),
                new TradingData("ETTS", System.currentTimeMillis(), 8465.1, 937)};

        SocketChannel sc = TestUtil.createTradingDataSocketChannel(datas);

        TradingDataReaderService tradingDataReaderService = new TradingDataReaderService();
        List<TradingData> received = new ArrayList<TradingData>();

        TradingData readData;
        while ((readData = tradingDataReaderService.receiveMessage(sc)) != null) {
            received.add(readData);
        }

        Assert.assertEquals("Trading data count", datas.length, received.size());
        for (int i = 0; i<datas.length; i++){
            Assert.assertEquals("Trading data["+i+"]", datas[i], received.get(i));
        }

    }

}
