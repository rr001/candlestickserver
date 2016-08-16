package my.test.exante.data;

import my.test.exante.util.Util;

import java.util.Calendar;

/**
 * Данные сделки
 */
public class TradingData  {
    /**
     * Тикер
     */
    private String ticker;
    /**
     * timestamp
     */
    private long timestamp;
    /**
     * Цена
     */
    private double price;
    /**
     * Количество
     */
    private int size;
    /**
     * timestamp обрезанный до минут
     */
    private long minutestamp;

    public TradingData(String ticker, long timestamp, double price, int size) {
        this.ticker = ticker;
        this.timestamp = timestamp;
        this.price = price;
        this.size = size;
        this.minutestamp = Util.getMinutes(timestamp);
    }

    public String getTicker() {
        return ticker;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public double getPrice() {
        return price;
    }

    public int getSize() {
        return size;
    }

    public long getMinutestamp() {
        return minutestamp;
    }

    public String toString() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(minutestamp * Util.MILLISECONDS_IN_MINUTE);
        // дата и время форматируется вручную, быстрее чем при использовании SimpleDateFormat
        String s = String.format("%04d-%02d-%02d %02d:%02d:%02d %s %f %d",
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH),
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                calendar.get(Calendar.SECOND),
                ticker,
                price,
                size
        );
        return s;
    }

    /**
     * Used only to compare to trading data instaces.
     * hashCode is not implemented beacause TradingDatas is not intended to be used as a key in hashtable or in hash sets.
     *
     * @param obj @see Object#equals(Object)
     * @return @see Object#equals(Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof TradingData)) return false;
        TradingData tradingData = (TradingData) obj;
        return ticker.equals(tradingData.ticker) &&
                timestamp == tradingData.timestamp &&
                price == tradingData.price &&
                size == tradingData.size;
    }
}
