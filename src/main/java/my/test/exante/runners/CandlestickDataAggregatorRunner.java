package my.test.exante.runners;

import my.test.exante.CandlestickServerException;
import my.test.exante.data.CandlestickData;
import my.test.exante.services.CandlestickDataAggregatorService;
import my.test.exante.data.TradingData;
import my.test.exante.util.RunnerBase;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.SortedSet;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Сбор биржевых данных и формирование свечей
 */
public class CandlestickDataAggregatorRunner extends RunnerBase {
    private static Logger log = LogManager.getLogger(CandlestickDataAggregatorRunner.class);

    /**
     * Очередь для получения биржевых данных
     */
    private BlockingQueue<TradingData> tradingDataQueue;

    /**
     * Очередь для отправки готовых свечей
     */
    private BlockingQueue<CandlestickData> candlestickDataQueue;

    /**
     * Сервис формирования свечей
     */
    private CandlestickDataAggregatorService candlestickDataAggregatorService;

    /**
     * Обработка цикла приема торговых данных, рачета свечей и отправке их потребителям.
     * Операции по обработке данных простые и быстрые, поэтому вся обработка проходит в одном потоке и не нужны какие либо межпоточные синхронизации
     * @throws Exception
     */
    @Override
    protected void execute() throws Exception {
        if (log.isTraceEnabled()) log.trace("enter");
        while (isRunning()) {
            // ожидаем данные из очереди
            // в качестве таймаута ожидания используем значение тацмаута агрегатора
            TradingData tradingData = tradingDataQueue.poll(candlestickDataAggregatorService.getTradingDataTimeOut(), TimeUnit.MILLISECONDS);

            // если торговые данные есть отправляем их в агрегатор
            if (tradingData != null) {
                if (log.isTraceEnabled()) log.trace(String.format("Trading data received: %s.", tradingData));
                candlestickDataAggregatorService.addTradingData(tradingData);
            }

            // даем агрегатору обработать накопивашиемя данные
            candlestickDataAggregatorService.processWaitinCandlesticks();

            // если есть свечи готовые к отправке, отправляем их в очередь потребителей.
            SortedSet<CandlestickData> readyCandlesticks = candlestickDataAggregatorService.getReadyCandleStickData();

            if (!readyCandlesticks.isEmpty()) {
                if (log.isTraceEnabled()) log.trace(String.format("%d candlesticks are ready. Send them to a queue.", readyCandlesticks.size()));
                for (CandlestickData candlestickData : readyCandlesticks) {
                    if (!candlestickDataQueue.offer(candlestickData, candlestickDataAggregatorService.getTradingDataTimeOut(), TimeUnit.SECONDS)) {
                        String message = String.format("Candlestick data output queue is full during %d milliseconds.", candlestickDataAggregatorService.getTradingDataTimeOut());
                        log.error(message);
                        throw new CandlestickServerException(message);
                    }
                }
                readyCandlesticks.clear();
            }

        }
        if (log.isTraceEnabled()) log.trace("leave");
    }

    public void setTradingDataQueue(BlockingQueue<TradingData> tradingDataQueue) {
        this.tradingDataQueue = tradingDataQueue;
    }

    public void setCandlestickDataQueue(BlockingQueue<CandlestickData> candlestickDataQueue) {
        this.candlestickDataQueue = candlestickDataQueue;
    }

    public void setCandlestickDataAggregatorService(CandlestickDataAggregatorService candlestickDataAggregatorService) {
        this.candlestickDataAggregatorService = candlestickDataAggregatorService;
    }

}
