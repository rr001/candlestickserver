package my.test.exante.runners;

import my.test.exante.util.CurrentTimeService;
import my.test.exante.data.TradingData;
import my.test.exante.services.TradingDataReaderService;
import my.test.exante.util.RunnerBase;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.channels.SocketChannel;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Сервис приема и парсинга поступающих торговых данных
 * Примем и разбор поступивших пакетов поисходит в @see TradingDataReader.
 * Разобранные пакеты (объекты @see TradingData) отправляются в очередь @see TradingDataReceiverRunner#tradingDataQueue
 */
public class TradingDataReceiverRunner extends RunnerBase {
    private static Logger log = LogManager.getLogger(TradingDataReceiverRunner.class);

    private CurrentTimeService currentTimeService;
    /**
     * Incoming trading data channel
     */
    private SocketChannel tradingDataSocketChannel;

    /**
     * @see my.test.exante.services.TradingDataReaderService
     * обеспечивает прием данных и сокета и разбор поступивших пакетов в @see TradingData
     */
    private TradingDataReaderService tradingDataReaderService;

    /**
     * Очередь для отправки разобранных пакетов
     */
    private BlockingQueue<TradingData> tradingDataQueue;

    /**
     * Сколько милисекунд ждать при отправке разобранного пакета в очередь.
     * Если время ожидания превышено, то прием торговых данных прекращается и потом останавливается.
     */
    private int queueTimeout = 5000;

    /**
     * Main execution method.
     *
     * @throws Exception any exception thrown during execution.
     */
    @Override
    protected void execute() throws Exception {
        if (log.isTraceEnabled()) log.trace("enter");

        tradingDataSocketChannel.configureBlocking(true);

        long startMinutes = currentTimeService.getCurrentTimeMinutes();

        TradingData tradingData = null;
        // Данные полученные в течении первой минут после запуска могут быть пропущена, поэтому игнорируем данные полученные в течении первой минуты.
        while (isRunning() && currentTimeService.getCurrentTimeMinutes() == startMinutes) {
            tradingData = tradingDataReaderService.receiveMessage(tradingDataSocketChannel);
            if (tradingData != null && tradingData.getMinutestamp() > startMinutes)
                break;
            else {
                tradingData = null;
                if (log.isTraceEnabled()) log.trace("First minute trading data ignored.");
            }
        }

        //  Осоновной цикл
        while (isRunning()) {
            if (tradingData != null) {
                log.trace(String.format("Trading data received %s.", tradingData));

                if (!tradingDataQueue.offer(tradingData, queueTimeout, TimeUnit.MILLISECONDS)) {
                    log.error(String.format("Trading data output queue is full during %d milliseconds. Stopping receiver.", queueTimeout));
                    break;
                }
            }

            tradingData = tradingDataReaderService.receiveMessage(tradingDataSocketChannel);
        }

        if (log.isTraceEnabled()) log.trace("leave");
    }

    public void setTradingDataSocketChannel(SocketChannel tradingDataSocketChannel) {
        this.tradingDataSocketChannel = tradingDataSocketChannel;
    }

    public void setTradingDataReaderService(TradingDataReaderService tradingDataReaderService) {
        this.tradingDataReaderService = tradingDataReaderService;
    }

    public void setTradingDataQueue(BlockingQueue<TradingData> tradingDataQueue) {
        this.tradingDataQueue = tradingDataQueue;
    }

    public void setQueueTimeout(int queueTimeout) {
        this.queueTimeout = queueTimeout;
    }


    public void setCurrentTimeService(CurrentTimeService currentTimeService) {
        this.currentTimeService = currentTimeService;
    }


}
