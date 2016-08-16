package my.test.exante.util;

/**
 * Вспомогательный класс для получения текущего времени.
 * Используется для:
 * - получения текущего времени способом отличным от System.currentTimeMillis()
 * - тестирования, чтобы не ждать минуты для завершения циклов обработки.
 */
public abstract class CurrentTimeService {
    /**
     * Возвращает текущее время
     *
     * @return текущее время в миллисекундах
     */
    public abstract long getCurrentTime();

    /**
     * Возвращает текущее время
     *
     * @return текущее время в минутах
     */
    public long getCurrentTimeMinutes() {
        return Util.getMinutes(getCurrentTime());
    }

}
