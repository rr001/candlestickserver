package my.test.exante.data;

import junit.framework.Assert;
import my.test.exante.CandlestickServerException;
import my.test.exante.util.Util;
import org.junit.Test;

public class CandlestickDataTest {
    public static String TICKER_1 = "ABC";
    public static String TICKER_2 = "SDFG";
    public static long timestamp = System.currentTimeMillis() - Util.MILLISECONDS_IN_MINUTE * 2;
    public static long minutestamp = timestamp / Util.MILLISECONDS_IN_MINUTE;
    public static double OPEN_PRICE_1 = 100D;
    public static double CLOSE_PRICE_1 = 150D;
    public static double MIN_PRICE_1 = 50D;
    public static double MAX_PRICE_1 = 200D;
    public static TradingData data11 = new TradingData(TICKER_1, timestamp, OPEN_PRICE_1, 10);
    public static TradingData data12 = new TradingData(TICKER_1, timestamp, MAX_PRICE_1, 20);
    public static TradingData data13 = new TradingData(TICKER_1, timestamp, MIN_PRICE_1, 20);
    public static TradingData data14 = new TradingData(TICKER_1, timestamp, CLOSE_PRICE_1, 20);
    public static int VOLUME1 = 10 + 20 + 20 + 20;

    public static TradingData data5 = new TradingData(TICKER_1, timestamp + Util.MILLISECONDS_IN_MINUTE, CLOSE_PRICE_1, 20);

    @Test
    public void testCandlestickData() throws CandlestickServerException {

        CandlestickData candlestickData = new CandlestickData(data11);
        candlestickData.addTradingData(data12);
        candlestickData.addTradingData(data13);
        candlestickData.addTradingData(data14);
        Assert.assertEquals("Ticker", TICKER_1, candlestickData.getTicker());
        Assert.assertEquals("Minutestamp", minutestamp, candlestickData.getMinutestamp());
        Assert.assertEquals("open price", OPEN_PRICE_1, candlestickData.getOpenPrice());
        Assert.assertEquals("close price", CLOSE_PRICE_1, candlestickData.getClosePrice());
        Assert.assertEquals("min price", MIN_PRICE_1, candlestickData.getMinPrice());
        Assert.assertEquals("min price", MAX_PRICE_1, candlestickData.getMaxPrice());
        Assert.assertEquals("volume", VOLUME1, candlestickData.getVolume());

        TradingData data5 = new TradingData("ABC", timestamp + Util.MILLISECONDS_IN_MINUTE, 300, 30);
        boolean exceptionThrown = false;
        try {
            candlestickData.addTradingData(data5);
        } catch (CandlestickServerException e) {
            exceptionThrown = true;
        }
        Assert.assertTrue("Exception expected", exceptionThrown);

    }

}
