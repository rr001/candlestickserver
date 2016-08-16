package my.test.exante.runners;

import my.test.exante.data.CandlestickData;
import my.test.exante.services.CandlestickDataDistributorService;
import my.test.exante.services.CandlestickDataHistoryService;
import my.test.exante.util.RunnerBase;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Поток обработки готовых свечей и отправки их потребителям
 */
public class CandlestickDataDistributorRunner extends RunnerBase {
    private static Logger log = LogManager.getLogger(CandlestickDataDistributorRunner.class);

    /**
     * Очередь для получения готовых свечей
     */
    private BlockingQueue<CandlestickData> candlestickDataQueue;

    /**
     * Сервис отправки свечей потребителям
     */
    private CandlestickDataDistributorService candlestickDataDistributorService;

    /**
     * Сервис сбора истории свечей
     */
    private CandlestickDataHistoryService candlestickDataHistoryService;

    /**
     * Сколько милисекунд ждать свечу из очереди.
     */
    private int queueTimeout = 1000;


    @Override
    protected void execute() throws Exception {
        while (isRunning()) {
            if (log.isTraceEnabled())
                log.trace(String.format("Waiting for candlestick from the queue."));
            CandlestickData candlestickData = candlestickDataQueue.poll(queueTimeout, TimeUnit.MILLISECONDS);

            if (candlestickData != null) {
                if (log.isTraceEnabled())
                    log.trace(String.format("Waiting for candlestick from the queue."));
                // добавляем свечу в историю
                candlestickDataHistoryService.addToHistory(candlestickData);

                // отправляем свечу потребителю
                candlestickDataDistributorService.distibute(candlestickData);
            }



            // отправка данных вновь подключенным потребителям
            candlestickDataDistributorService.processNewConsumers(candlestickDataHistoryService);

            // очиска каналов потребителей, у которых произошла ошибка
            candlestickDataDistributorService.cleanup();
        }


    }

    public BlockingQueue<CandlestickData> getCandlestickDataQueue() {
        return candlestickDataQueue;
    }

    public void setCandlestickDataQueue(BlockingQueue<CandlestickData> candlestickDataQueue) {
        this.candlestickDataQueue = candlestickDataQueue;
    }

    public CandlestickDataDistributorService getCandlestickDataDistributorService() {
        return candlestickDataDistributorService;
    }

    public void setCandlestickDataDistributorService(CandlestickDataDistributorService candlestickDataDistributorService) {
        this.candlestickDataDistributorService = candlestickDataDistributorService;
    }

    public void setCandlestickDataHistoryService(CandlestickDataHistoryService candlestickDataHistoryService) {
        this.candlestickDataHistoryService = candlestickDataHistoryService;
    }
}
