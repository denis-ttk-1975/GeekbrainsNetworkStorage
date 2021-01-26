package netty;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentLinkedDeque;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class EchoHandler extends SimpleChannelInboundHandler<String> {

    private static ConcurrentLinkedDeque<ChannelHandlerContext> clients = new ConcurrentLinkedDeque<>();

    private String name;
    private static int cnt = 0;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        clients.add(ctx);
        cnt++;
        name = "user#" + cnt;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String s) throws Exception {
        LocalDateTime time = LocalDateTime.now();
        for (ChannelHandlerContext client : clients) {
            client.writeAndFlush("[" + time.toString() + "]" + name + ": " + s);
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        clients.remove(ctx);
    }
}
