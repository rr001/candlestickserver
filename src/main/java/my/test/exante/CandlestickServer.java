package my.test.exante;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.File;
import java.io.IOException;

/**
 * главный класс
 */
public class CandlestickServer {
    static ClassPathXmlApplicationContext appCtx;
    public static void main(String[] args) throws InterruptedException, IOException {
        System.out.println(new File(".").getAbsolutePath());
        appCtx = new ClassPathXmlApplicationContext("/context.xml");

        appCtx.registerShutdownHook();

        while(!appCtx.isRunning()) Thread.sleep(500);

        System.out.println("server is running.");

        while(appCtx.isRunning()) {
            char chr = (char) System.in.read();
            if (chr==3) break;
            Thread.sleep(500);
        }
        System.out.println("stopping server...");

//        System.exit(0);



    }

}
