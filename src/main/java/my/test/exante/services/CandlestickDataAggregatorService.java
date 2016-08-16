package my.test.exante.services;

import my.test.exante.CandlestickServerException;
import my.test.exante.data.CandlestickData;
import my.test.exante.data.TradingData;
import my.test.exante.util.CurrentTimeService;
import my.test.exante.util.Util;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Агрегирует торговые данные в свечи.
 */
public class CandlestickDataAggregatorService {
    private static Logger log = LogManager.getLogger(CandlestickDataAggregatorService.class);
    /**
     * Получение текущего времени
     */
    @Autowired
    private CurrentTimeService currentTimeService;
    /**
     * Candlesticks beeing prepared.
     */
    private Map<String, CandlestickData> candlestickDataByTicker = new HashMap<String, CandlestickData>();

    /**
     * Счечи готовые к отправке потребителям.
     */
    private SortedSet<CandlestickData> readyCandleStickData = new TreeSet<CandlestickData>();

    /**
     * Сколько (миллисекунд), в течении следующей минуты нужно ждать поступления новых торговых данных, перед тем как отметить свечу, как готовую к отправке потребителям.
     */
    private long tradingDataTimeOut = 10000;


    public void addTradingData(TradingData data) throws CandlestickServerException {
        if (log.isTraceEnabled())
            log.trace(String.format("enter. Trading ticker=%s, munites=%d", data.getTicker(), data.getMinutestamp()));

        CandlestickData candlestickData = candlestickDataByTicker.get(data.getTicker());
        if (candlestickData == null) {
            candlestickData = new CandlestickData(data);
            candlestickDataByTicker.put(candlestickData.getTicker(), candlestickData);
        } else {
            // если по данному тикеру уже есть свеча за предыдубщие минуты, то переводим ее в готовые к отправке
            if (data.getMinutestamp() > candlestickData.getMinutestamp()) {
                //
                if (log.isTraceEnabled())
                    log.trace(String.format("Candlestick package is ready: %s.", candlestickData));
                readyCandleStickData.add(candlestickData);
                candlestickData = new CandlestickData(data);
                candlestickDataByTicker.put(candlestickData.getTicker(), candlestickData);
            } else {
                candlestickData.addTradingData(data);
            }
        }
        if (log.isTraceEnabled()) log.trace("leave");
    }

    /**
     * Определяет нужно ли еще ждать биржевые данные по заданой свече
     *
     * @param candlestickData свеча
     * @return true-свеча готова к отправке, false-ожидаются еще биржевые данные
     */
    private boolean isCandlestickTimedOut(CandlestickData candlestickData) {
        long currentTimeMillis = currentTimeService.getCurrentTime();
        return (Util.getMilliseconds(candlestickData.getMinutestamp() + 1)) + tradingDataTimeOut <= currentTimeMillis;
    }

    /**
     * Переводит свечи из ожидающих новых данных в готовые для отправки.
     */
    public void processWaitinCandlesticks() {
        if (log.isTraceEnabled()) log.trace("enter");

        List<CandlestickData> candlesticksPending = new ArrayList<CandlestickData>(candlestickDataByTicker.values());
        for (CandlestickData candlestick : candlesticksPending) {
            // check if candlestick data timedout
            if (isCandlestickTimedOut(candlestick)) {
                if (log.isTraceEnabled())
                    log.trace(String.format("Candlestick package is ready: %s.", candlestick));
                // add candlestickdata to ready list
                readyCandleStickData.add(candlestick);
                // remove ready candle
                candlestickDataByTicker.remove(candlestick.getTicker());
            }
        }
        if (log.isTraceEnabled()) log.trace("leave");
    }

    public SortedSet<CandlestickData> getReadyCandleStickData() {
        return readyCandleStickData;
    }

    public void clear() {
        if (log.isTraceEnabled()) log.trace("enter");
        readyCandleStickData.clear();
    }

    public CurrentTimeService getCurrentTimeService() {
        return currentTimeService;
    }

    public void setCurrentTimeService(CurrentTimeService currentTimeService) {
        this.currentTimeService = currentTimeService;
    }

    public long getTradingDataTimeOut() {
        return tradingDataTimeOut;
    }

    public void setTradingDataTimeOut(long tradingDataTimeOut) {
        this.tradingDataTimeOut = tradingDataTimeOut;
    }
}
