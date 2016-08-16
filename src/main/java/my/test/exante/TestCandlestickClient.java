package my.test.exante;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.Socket;

/**
 * Тестовый клиент для считывания свечей
 */
public class TestCandlestickClient {

    public static void main(String[] args) throws IOException {
        System.out.println("Test candlestick client\nUsage: testclient [host [port]]\n" +
                "Defaults: host=127.0.0.1, port=5556");
        String host = "127.0.0.1";
        int port = 5556;
        if (args.length > 0)
            host = args[0];
        if (args.length > 1)
            port = Integer.parseInt(args[1]);


        Socket socket = new Socket(host, port);

        InputStream ins = socket.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(ins,"ascii"));
        while(true) {
            String s = reader.readLine();
            System.out.println(s);
        }

    }
}
