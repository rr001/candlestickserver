package my.test.exante.services;

import my.test.exante.data.CandlestickData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.channels.SocketChannel;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.concurrent.ExecutorService;

public class CandlestickDataDistributorService {
    private static Logger log = LogManager.getLogger(CandlestickDataDistributorService.class);

    private CandlestickDataHistoryService candlesticksHistory;

    private ExecutorService executorService;

    private Object monitor = new Object();

    /**
     * Подключенные потребители, которым нужно отправлять только новые свечи
     */
    private Set<CandlestickDistributionChannel> distributionChannels = new HashSet<CandlestickDistributionChannel>();

    /**
     * Вновью подключившиеся потребители, которым требуется отправить историю свечей.
     */
    private Set<CandlestickDistributionChannel> newDistributionChannels = new HashSet<CandlestickDistributionChannel>();


    /**
     * Отправить свечу потребителям, которые уде подключены больше одной минуты
     *
     * @param candlestick свеча
     */
    public void distibute(CandlestickData candlestick) {
        if (log.isTraceEnabled())
            log.trace(String.format("Sending candlestick %s to consumers", candlestick));
        Set<CandlestickDistributionChannel> consumers = null;
        synchronized (monitor) {
            if (!distributionChannels.isEmpty())
                consumers = new HashSet<CandlestickDistributionChannel>(distributionChannels);
        }
        if (consumers != null) {
            for (CandlestickDistributionChannel consumer : consumers) {
                if (log.isTraceEnabled())
                    log.trace(String.format("Sending candlestick %s to consumer %s", candlestick, consumer));
                consumer.getCandlestickDistributionQueue().add(candlestick);
                executeDistribution(consumer);
            }
        }
    }

    /**
     * Запустить отправку. Поток отправки через в @see java.util.concurrent.ExecutorService
     *
     * @param consumer канал отправки свечей потребителю
     */
    private void executeDistribution(CandlestickDistributionChannel consumer) {
        if (log.isTraceEnabled())
            log.trace(String.format("Executing distribution to consumer %s", consumer));
        if (consumer.isIdle()) {
            if (log.isTraceEnabled())
                log.trace(String.format("Consumer %s distribution is pending to be executed.", consumer));
            executorService.submit(consumer);
        }
    }


    /**
     * Удаление каналов отправки, в которых произошли ошибки (например клиент закрыл соединение).
     */
    public void cleanup() {
        synchronized (monitor) {
            Iterator<CandlestickDistributionChannel> iter = distributionChannels.iterator();
            while (iter.hasNext()) {
                CandlestickDistributionChannel cdc = iter.next();
                if (cdc.isError()) {
                    if (log.isTraceEnabled())
                        log.trace(String.format("Cleaning distribution channel %s, because of error %s", cdc, cdc.getException()));
                    iter.remove();
                }
            }
        }
    }

    /**
     * Подключение нового потребителя
     *
     * @param clientChannel сокет для отправки данных
     * @return @CandlestickDistributionChannel
     */
    public CandlestickDistributionChannel createNewDistributionChannel(SocketChannel clientChannel) {
        if (log.isTraceEnabled()) log.trace("Add distribution channel.");
        synchronized (monitor) {
            CandlestickDistributionChannel dc = new CandlestickDistributionChannel(clientChannel);
            newDistributionChannels.add(dc);
            return dc;
        }
    }

    /**
     * Отправка истории свечей за @see CandlestickDataDistributorService#candlesticksHistoryDepth последних минут вновь подключенным потребителям.
     *
     * @param historyService
     */
    public void processNewConsumers(CandlestickDataHistoryService historyService) {
        // делаем копию списка вновь подключенных клиентов потокобезопасно
        SortedMap<Long, List<CandlestickData>> actualHistory = historyService.getActualHistory();

        Set<CandlestickDistributionChannel> newConsumers;
        newConsumers = null;
        synchronized (monitor) {
            if (!newDistributionChannels.isEmpty())
                newConsumers = new HashSet<>(newDistributionChannels);
        }
        // если есть вновь подключенные клиенты - оправляем им историю
        if (newConsumers != null) {
            if (log.isTraceEnabled()) log.trace("Distributing candlestick history to new consumers.");
            for (CandlestickDistributionChannel cdc : newConsumers) {
                for (List<CandlestickData> candlesticks : actualHistory.values()) {
                    cdc.getCandlestickDistributionQueue().addAll(candlesticks);
                }
                executeDistribution(cdc);
            }
            // перемещаем вновь подключенных потребителей в список уже существоваших потребителей
            synchronized (monitor) {
                newDistributionChannels.removeAll(newConsumers);
                distributionChannels.addAll(newConsumers);
            }
        }
    }

    public void setExecutorService(ExecutorService executorService) {
        this.executorService = executorService;
    }

    public CandlestickDataHistoryService getCandlesticksHistory() {
        return candlesticksHistory;
    }

    public void setCandlesticksHistory(CandlestickDataHistoryService candlesticksHistory) {
        this.candlesticksHistory = candlesticksHistory;
    }
}
