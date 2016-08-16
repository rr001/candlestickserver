package my.test.exante.services;

import my.test.exante.data.CandlestickData;
import my.test.exante.util.CurrentTimeService;
import my.test.exante.util.Util;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Сборщик свечей.
 * Не threadsafe. При вызове методов нужна синхронизация.
 */
public class CandlestickDataHistoryService {
    private static Logger log = LogManager.getLogger(CandlestickDataHistoryService.class);

    private CurrentTimeService currentTimeService;

    /**
     * Сколько минут держать свечи в истории
     */
    private int candlesticksHistoryDepth = 10;

    /**
     * Исторические свечи, разбитые по минутам, отсортированные по времеи от старых к новым
     */
    private SortedMap<Long, List<CandlestickData>> candlesticksHistory = new TreeMap<Long, List<CandlestickData>>();

    /**
     * Время, когда последний раз удалялись старые (@see CandlestickDataHistoryService#candlesticksHistoryDepth)  свечи.
     */
    private long lastCleaningTime = 0;


    /**
     * Возвращает актуальныю исптоию свечей, не включая свечи старще чем @see candlesticksHistoryDepth минут
     *
     * @return SortedMap со свечами отсортированные по времени свечи, от старых к новым
     */
    public SortedMap<Long, List<CandlestickData>> getActualHistory() {
        // чистим историю от старых свечей
        long currentTime = currentTimeService.getCurrentTime();
        // очистку делаем не чаще одного раза в минуту
        if (Util.getMinutes(currentTime - lastCleaningTime) > 0) {
            long minutesToRemoveFromHistory = Util.getMinutes( currentTime) - candlesticksHistoryDepth+1;
            SortedMap newActualHostory = candlesticksHistory.tailMap(minutesToRemoveFromHistory);
            candlesticksHistory = new TreeMap(newActualHostory);
            lastCleaningTime = currentTime;
        }
        return candlesticksHistory;
    }


    /**
     * Добавить свечу в историю
     *
     * @param candlestickData свеча
     */
    public void addToHistory(CandlestickData candlestickData) {
        if (log.isTraceEnabled()) log.trace(String.format("adding candlestick: %s", candlestickData));
        long candlestickMinutes = candlestickData.getMinutestamp();
        List<CandlestickData> candlesticks = candlesticksHistory.get(candlestickMinutes);
        if (candlesticks == null) {
            candlesticks = new ArrayList<CandlestickData>(10);
            candlesticksHistory.put(candlestickMinutes, candlesticks);
        }
        if (candlesticks.contains(candlestickData)) {
            log.warn(String.format("Duplicated candlestick ignored: %s", candlestickData));
        } else {
            candlesticks.add(candlestickData);
        }

    }

    public void setCurrentTimeService(CurrentTimeService currentTimeService) {
        this.currentTimeService = currentTimeService;
    }

    public int getCandlesticksHistoryDepth() {
        return candlesticksHistoryDepth;
    }

    public void setCandlesticksHistoryDepth(int candlesticksHistoryDepth) {
        this.candlesticksHistoryDepth = candlesticksHistoryDepth;
    }
}
