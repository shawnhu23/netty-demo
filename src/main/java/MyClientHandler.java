import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class MyClientHandler extends ChannelInboundHandlerAdapter {

    private final BufferedReader bufferedReader;

    public MyClientHandler() {
        this.bufferedReader = new BufferedReader(new InputStreamReader(System.in));
    }

    @Override
    public void channelActive(ChannelHandlerContext context) {
        try {
            while (true) {
                String message = bufferedReader.readLine();
                if (message == null || message.equalsIgnoreCase("exit")) {
                    context.close();
                    break;
                }

                ByteBuf buf = context.alloc().buffer();
                buf.writeBytes(message.getBytes());
                context.writeAndFlush(buf);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext context) {
        context.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext context, Throwable cause) {
        cause.printStackTrace();
        context.close();
    }
}
