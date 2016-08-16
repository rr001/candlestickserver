package my.test.exante.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class RunnableBase implements Runnable {
    private static Logger log = LogManager.getLogger(RunnableBase.class);
    public static final int STATUS_RUNNING = 1;
    public static final int STATUS_ERROR = -1;
    public static final int STATUS_IDLE = 2;

    private volatile int status = STATUS_IDLE;

    private Exception exception;

    public boolean isRunning() {
        int __status = status;
        return __status == STATUS_RUNNING;
    }

    public boolean isIdle() {
        int __status = status;
        return __status == STATUS_IDLE;
    }

    public boolean isError() {
        int __status = status;
        return __status == STATUS_ERROR;
    }

    protected void setError(Exception e) {
        status = STATUS_ERROR;
        exception = e;
    }

    public void stop() {
        log.trace(String.format( "Stopping thread %s", this));
        if (isRunning())
            setIdle();
        else
            throw new IllegalStateException("Runner not in running state, cannot be stopped.");
    }

    protected void setRunning() {
        status = STATUS_RUNNING;
    }

    protected void setIdle() {
        status = STATUS_IDLE;
    }


    public Exception getException() {
        return exception;
    }
}
