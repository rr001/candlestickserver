package my.test.exante.services;

import junit.framework.Assert;
import my.test.exante.data.CandlestickData;
import my.test.exante.data.TradingData;
import my.test.exante.util.CurrentTimeService;
import my.test.exante.util.Util;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.SortedMap;
import java.util.TreeMap;

public class CandlestickDataHistoryServiceTest {
    private static class CurrentTimeTestService extends CurrentTimeService {
        private long currentTime;
        private Random rnd = new Random();

        @Override
        public long getCurrentTime() {
            return currentTime;
        }

        public void setCurrentTime(long currentTime) {
            this.currentTime = currentTime;
        }

        public void setCurrentTimeMinutes(int minutes) {
            setCurrentTime(0);
            incMinutes(minutes);
        }

        /**
         * Добавляет к текущему времени заданное количество минут и случайно количество милисекунд в пределах однйо минуты
         *
         * @param minutesToIncrement
         */
        public long incMinutes(int minutesToIncrement) {
            setCurrentTime(Util.getMilliseconds(getCurrentTimeMinutes() + minutesToIncrement) + rnd.nextInt(Util.MILLISECONDS_IN_MINUTE));
            return currentTime;
        }
    }

    CandlestickData createCandlestick(String ticker, long time) {
        CandlestickData candlestick = new CandlestickData(new TradingData(ticker, time, 100.0, 10));
        return candlestick;
    }

    SortedMap<Long, List<CandlestickData>> createActualHistory(List<CandlestickData>... lists) {
        SortedMap<Long, List<CandlestickData>> actualHistory = new TreeMap<Long, List<CandlestickData>>();
        for (List<CandlestickData> list : lists) {
            long minutes = list.get(0).getMinutestamp();
            actualHistory.put(minutes, list);
        }
        return actualHistory;
    }

    List<CandlestickData> createList(CandlestickData... datas) {
        List<CandlestickData> list = new ArrayList<CandlestickData>();
        for (CandlestickData data : datas)
            list.add(data);
        return list;
    }


    @Test
    public void testCandlestickDataHistoryService() {
        CurrentTimeTestService currentTimeService = new CurrentTimeTestService();
        CandlestickDataHistoryService historyService = new CandlestickDataHistoryService();
        historyService.setCurrentTimeService(currentTimeService);
        historyService.setCurrentTimeService(currentTimeService);

        currentTimeService.setCurrentTimeMinutes(1);


        SortedMap<Long, List<CandlestickData>> ACTUALHISTORY =
                createActualHistory(
                        createList(
                                createCandlestick("AA", currentTimeService.getCurrentTime()),
                                createCandlestick("BBA", currentTimeService.getCurrentTime())),
                        createList(
                                createCandlestick("AA", currentTimeService.incMinutes(1)),
                                createCandlestick("BBA", currentTimeService.getCurrentTime()))

                );

        for (List<CandlestickData> list : ACTUALHISTORY.values()) {
            for (CandlestickData candlestickData : list) {
                historyService.addToHistory(candlestickData);
            }
        }

        SortedMap<Long, List<CandlestickData>> actualHistory = historyService.getActualHistory();

        Assert.assertEquals(ACTUALHISTORY, actualHistory);


        currentTimeService.incMinutes(1);


        ACTUALHISTORY = new TreeMap<Long, List<CandlestickData>>();

        for (int i = 0; i < 10; i++) {
            currentTimeService.incMinutes(1);
            List<CandlestickData> list =
                    createList(
                            createCandlestick("AA", currentTimeService.getCurrentTime()),
                            createCandlestick("AC", currentTimeService.getCurrentTime()),
                            createCandlestick("BBA", currentTimeService.getCurrentTime()));

            ACTUALHISTORY.put(currentTimeService.getCurrentTimeMinutes(), list);
            for (CandlestickData candlestickData : list)
                historyService.addToHistory(candlestickData);
        }
        Assert.assertEquals(ACTUALHISTORY, historyService.getActualHistory());

        System.out.println(ACTUALHISTORY);
    }
}
