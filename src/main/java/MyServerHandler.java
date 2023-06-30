import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MyServerHandler extends ChannelInboundHandlerAdapter {
    private final int clientId;
    private ExecutorService executorService;

    public MyServerHandler(int clientId) {
        this.clientId = clientId;
        executorService = Executors.newFixedThreadPool(10);
    }

    // the use of executorService and completableFuture is only to demonstrate its usage
    @Override
    public void channelRead(ChannelHandlerContext context, Object msg) {
        // cast the object into ByteBuf and convert it into String using UTF-8 encoding
        ByteBuf buf = (ByteBuf) msg;
        String message = buf.toString(StandardCharsets.UTF_8);
        // release to deallocate ByteBuf to ensure proper memory management and prevent memory leak
        buf.release();

        // Processing the received message asynchronously
        CompletableFuture<String> processingFuture = CompletableFuture.supplyAsync(() -> {
            // we could do some time-consuming or blocking operation here
            String result = processMessage(context, message);
            return result;
        }, executorService);

        // Handle the result asynchronously using another completableFuture
        CompletableFuture<Void> resultHandlingFuture = processingFuture.thenAcceptAsync(result -> {
            System.out.println(result);
        }, executorService);

        // Handle any exception
        resultHandlingFuture.exceptionally(throwable -> {
            throwable.printStackTrace();
            return null;
        });
    }

    private String processMessage(ChannelHandlerContext context, String message) {
        return "Received message from Client " + clientId + ": " + message;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext context, Throwable cause) {
        cause.printStackTrace();
        context.close();
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext context) {
        context.flush();
    }

    @Override
    public void channelInactive(ChannelHandlerContext context) {
        System.out.println("Client " + clientId + " disconnected.");
        executorService.shutdown();
    }
}
