package my.test.exante.services;

import junit.framework.Assert;
import my.test.exante.CandlestickServerException;
import my.test.exante.data.CandlestickDataTest;
import my.test.exante.util.CurrentTimeService;
import my.test.exante.util.CurrentTimeServiceImpl;
import org.junit.Test;

public class CandlestickDataAggregatorServiceTest {


    @Test
    public void testAddTradingData() throws CandlestickServerException {

        CurrentTimeService currentTimeService = new CurrentTimeServiceImpl();

        CandlestickDataAggregatorService candlestickDataAggregatorService = new CandlestickDataAggregatorService();
        candlestickDataAggregatorService.setCurrentTimeService(currentTimeService);
        candlestickDataAggregatorService.addTradingData(CandlestickDataTest.data11);
        candlestickDataAggregatorService.addTradingData(CandlestickDataTest.data12);
        candlestickDataAggregatorService.addTradingData(CandlestickDataTest.data13);
        candlestickDataAggregatorService.addTradingData(CandlestickDataTest.data14);

        Assert.assertEquals("ready candlestick count", 0, candlestickDataAggregatorService.getReadyCandleStickData().size());

        candlestickDataAggregatorService.addTradingData(CandlestickDataTest.data5);

        Assert.assertEquals("ready candlestick count=1", 1, candlestickDataAggregatorService.getReadyCandleStickData().size());

        candlestickDataAggregatorService.processWaitinCandlesticks();

        Assert.assertEquals("ready candlestick count=1", 2, candlestickDataAggregatorService.getReadyCandleStickData().size());

    }
}
