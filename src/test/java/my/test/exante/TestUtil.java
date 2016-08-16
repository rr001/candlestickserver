package my.test.exante;

import my.test.exante.data.TradingData;
import my.test.exante.services.TradingDataReaderService;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class TestUtil {
    public static ByteBuffer createTradingByteBuffer(TradingData... tradingDatas) throws UnsupportedEncodingException {
        ByteBuffer buffer = ByteBuffer.allocate(TradingDataReaderService.BUFFER_SIZE * tradingDatas.length);
        int offset = 0;
        for (TradingData tradingData : tradingDatas) {
            buffer.position(offset + 2);
            buffer.putLong(tradingData.getTimestamp());
            byte[] tickerBytes = tradingData.getTicker().getBytes("ascii");
            buffer.putShort((short) tickerBytes.length);
            buffer.put(tickerBytes);
            buffer.putDouble(tradingData.getPrice());
            buffer.putInt(tradingData.getSize());

            short messageSize = (short) (buffer.position() - 2 - offset);
            buffer.position(offset);
            buffer.putShort(messageSize);
            offset += messageSize+2;
        }
        buffer.limit(offset);
        buffer.position(0);
        return buffer;
    }

    public static SocketChannel createSocketChannel(final ByteBuffer buffer) throws IOException {
        SocketChannel socketChannel = Mockito.mock(SocketChannel.class);

        Answer<Integer> answer = new Answer<Integer>() {
            @Override
            public Integer answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                ByteBuffer bb = (ByteBuffer) args[0];
                int targetPosition = bb.position();
                while (bb.position() < bb.limit() && buffer.position()<buffer.limit())
                    bb.put(buffer.get());
                int writtenBytes = bb.position()-targetPosition;
                return writtenBytes;
            }
        };

        Mockito.when(socketChannel.read(Mockito.any(ByteBuffer.class))).thenAnswer(answer);

        return socketChannel;
    }

    public static SocketChannel createTradingDataSocketChannel(TradingData... datas) throws IOException {
        ByteBuffer buffer = createTradingByteBuffer(datas);

        SocketChannel socketChannel = createSocketChannel(buffer);

        return socketChannel;
    }


}
