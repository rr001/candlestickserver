package my.test.exante.data;

import my.test.exante.CandlestickServerException;
import my.test.exante.util.Util;

import java.util.Calendar;

/**
 * Свеча.
 */
public class CandlestickData implements Comparable<CandlestickData> {
    private double openPrice;
    private double closePrice;
    private double minPrice;
    private double maxPrice;
    private String ticker;
    private int volume;
    private long minutestamp;

    public CandlestickData(TradingData tradingData) {
        this.ticker = tradingData.getTicker();
        openPrice = tradingData.getPrice();
        closePrice = tradingData.getPrice();
        minPrice = tradingData.getPrice();
        maxPrice = tradingData.getPrice();
        volume = tradingData.getSize();
        this.minutestamp = tradingData.getMinutestamp();
    }

    public void addTradingData(TradingData tradingData) throws CandlestickServerException {
        if (tradingData.getMinutestamp() != minutestamp)
            throw new CandlestickServerException("Trading data is out of time.");
        closePrice = tradingData.getPrice();
        if (minPrice > tradingData.getPrice()) minPrice = tradingData.getPrice();
        if (maxPrice < tradingData.getPrice()) maxPrice = tradingData.getPrice();
        volume += tradingData.getSize();
    }

    public double getOpenPrice() {
        return openPrice;
    }

    public double getClosePrice() {
        return closePrice;
    }

    public double getMinPrice() {
        return minPrice;
    }

    public double getMaxPrice() {
        return maxPrice;
    }

    public String getTicker() {
        return ticker;
    }

    public int getVolume() {
        return volume;
    }

    public String getJson() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(minutestamp * Util.MILLISECONDS_IN_MINUTE);
        String json = String.format("{ \"ticker\": \"%s\", \"timestamp\": \"%04d-%02d-%02dT%02d:%02d:%02dZ\", \"open\": %f, \"high\": %f, \"low\": %f, \"close\": %f, \"volume\": %d }",
                ticker,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH),
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                calendar.get(Calendar.SECOND),
                openPrice,
                maxPrice,
                minPrice,
                closePrice,
                volume);
        return json;
    }

    public String toString() {
        return getJson();
    }

    public long getMinutestamp() {
        return minutestamp;
    }

    /**
     * Used to sort candlesticks according to it's minutes timestamp
     *
     * @param o candlestick to compare to
     * @return @see Comparable#compareTo(Object)
     */
    @Override
    public int compareTo(CandlestickData o) {
        long l1 = minutestamp - o.minutestamp;
        if (l1 == 0)
            return ticker.compareTo(o.ticker);
        return l1 > 0 ? 1 : -1;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o instanceof CandlestickData)
            return compareTo((CandlestickData) o) == 0;
        return false;
    }

    @Override
    public int hashCode() {
        int result = ticker.hashCode();
        // сгенерировано идеей
        result = 31 * result + (int) (minutestamp ^ (minutestamp >>> 32));
        return result;
    }
}
