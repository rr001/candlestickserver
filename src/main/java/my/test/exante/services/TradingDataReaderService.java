package my.test.exante.services;

import my.test.exante.CandlestickServerException;
import my.test.exante.data.TradingData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * Считывает и парсит торговые данные из сокета.
 * Не threadsafe. Метод @see TradingDataReader# receiveMessage(SocketChannel) может выполняться только в одном потоке.
 */
public class TradingDataReaderService {
    private static Logger log = LogManager.getLogger(TradingDataReaderService.class);

    /**
     * Максимальная длина тикера во вхадящем пакете
     */
    public static final int MAX_TICKER_LENGTH = 10;
    /**
     * Минимальная длина тикера во вхадящем пакете
     */
    public static final int MIN_TICKER_LENGTH = 1;
    /**
     * Размер сообщения минус тикер
     */
    public static final int BASE_MESSAGE_SIZE = 22;
    /**
     * Максимальный размер сообщения
     */
    public static final int MAX_MESSAGE_LENGTH = BASE_MESSAGE_SIZE + MAX_TICKER_LENGTH;
    /**
     * Минимальный размер сообщения
     */
    public static final int MIN_MESSAGE_LENGTH = BASE_MESSAGE_SIZE + MIN_TICKER_LENGTH;

    /**
     * Размер буфера для приема сообщения
     */
    public static final int BUFFER_SIZE = MAX_MESSAGE_LENGTH + 2;
    /**
     * Буффер для приема входящих пакетов, включая
     */
    private ByteBuffer messageBuffer = ByteBuffer.allocate(BUFFER_SIZE);


    /**
     * Считывает данные из SocketChannel и парсит их
     *
     * @param tradingDataSocketChannel канал получения торговых данных
     * @return TradingData торговые данные, или null, если данных не поступило
     * @throws IOException          выбрасывается в случае возникноверния ошибок ввода
     * @throws my.test.exante.CandlestickServerException выбрасывается в случае, если входящий пакет не может быть рапарсен
     */
    public TradingData receiveMessage(SocketChannel tradingDataSocketChannel) throws IOException, CandlestickServerException {
        if (log.isTraceEnabled()) log.trace("enter");
        messageBuffer.position(0);
        messageBuffer.limit(2);

        // получаем длину сообщения
        int receivedBytes = tradingDataSocketChannel.read(messageBuffer);

        // если во входящем канале нет данных, позвращаем null
        if (receivedBytes == 0)
            return null;

        // получаем оставщийся байт длинны сообщения, если нужно
        if (receivedBytes < 2)
            while ((receivedBytes += tradingDataSocketChannel.read(messageBuffer)) < 2) ;

        messageBuffer.position(0);

        int messageLength = messageBuffer.getShort();

        // проверяем длину сообщения
        if (messageLength > MAX_MESSAGE_LENGTH) {
            String errorMessage = String.format("Message size is bigger that expected, received %d bytes, expected not more that %d", messageLength, MAX_MESSAGE_LENGTH);
            log.error(errorMessage);
            throw new CandlestickServerException(errorMessage);
        }
        if (messageLength < MIN_MESSAGE_LENGTH) {
            String errorMessage = String.format("Message size is lower that expected, received %d bytes, expected not less that %d", messageLength, MIN_MESSAGE_LENGTH);
            log.error(errorMessage);
            throw new CandlestickServerException(errorMessage);
        }
        if (log.isTraceEnabled()) log.trace(String.format("Message length: %d", messageLength));

        // получаем оставшуююся часть пакета
        messageBuffer.limit(2 + messageLength);
        receivedBytes = 0;
        while ((receivedBytes += tradingDataSocketChannel.read(messageBuffer)) < messageLength) ;

        // парсим пакет
        messageBuffer.position(2);
        long timestamp = messageBuffer.getLong();
        int tickerSize = messageBuffer.getShort();
        byte[] bTicker = new byte[tickerSize];
        messageBuffer.get(bTicker);
        String ticker = new String(bTicker, "ascii");
        double price = messageBuffer.getDouble();
        int size = messageBuffer.getInt();
        TradingData data = new TradingData(ticker, timestamp, price, size);
        if (log.isTraceEnabled()) log.trace(String.format("Trading data received: %s", data));

        if (log.isTraceEnabled()) log.trace("leave");

        return data;
    }
}
