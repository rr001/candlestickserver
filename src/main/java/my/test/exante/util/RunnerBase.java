package my.test.exante.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class RunnerBase extends RunnableBase {
    private static Logger log = LogManager.getLogger(RunnerBase.class);
    private Thread runnerThread;

    @Override
    public final void run() {
        if (isRunning())
            return;
        setRunning();
        try {
            execute();
            setIdle();
        } catch (Exception e) {
            log.error("Stopped because of exception.", e);
            setError(e);
        }
    }

    protected abstract void execute() throws Exception;

    public synchronized void startRunner() {
        log.trace("Starting runners thread");
        if (runnerThread != null) {
            log.trace("Runner thread is already started");
        }

        runnerThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    RunnerBase.this.run();
                } finally {
                    synchronized (RunnerBase.this) {
                        log.trace("Clear runner thread");
                        runnerThread = null;
                    }
                }
            }
        });
        runnerThread.start();

    }

    public synchronized void stopRunner() {
        log.trace("Stopping runners thread..");
        try {
            stop();
            runnerThread.interrupt();
            runnerThread.join(100);
        } catch (Exception e) {
            log.warn("Error stopping runner thread", e);
        } finally {
            runnerThread = null;
        }
        log.trace("Runner thread stopped.");
    }
}
