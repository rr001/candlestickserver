package my.test.exante.util;

/**
 * Вспомогательные функции
 */
public class Util {
    public static final int MILLISECONDS_IN_MINUTE = 60000;

    /**
     * Преобразует вмея в миллисекундах в минут
     * @param milliseconds время в миллисекундах
     * @return время в минутах
     */
    public static long getMinutes(long milliseconds) {
        return milliseconds / MILLISECONDS_IN_MINUTE;
    }

    /**
     * Преобразует время в минутах во время в миллисекундах
     * @param minutes время в минутах
     * @return время в миллисекундах
     */
    public static long getMilliseconds(long minutes) {
        return minutes * MILLISECONDS_IN_MINUTE;
    }
}
