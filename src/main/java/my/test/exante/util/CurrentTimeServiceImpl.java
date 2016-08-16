package my.test.exante.util;

import my.test.exante.util.CurrentTimeService;
import my.test.exante.util.Util;

/**
 * Дефолтная реализация @see CurrentTimeService.
 */
public class CurrentTimeServiceImpl extends CurrentTimeService {
    /**
     * @see my.test.exante.util.CurrentTimeService#getCurrentTime()
     * Использует System.currentTimeMillis();
     */
    @Override
    public long getCurrentTime() {
        return System.currentTimeMillis();
    }

}
