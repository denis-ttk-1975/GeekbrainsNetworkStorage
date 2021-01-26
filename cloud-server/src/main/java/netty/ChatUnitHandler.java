package netty;

import java.util.concurrent.ConcurrentLinkedDeque;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import model.ChatUnitMessage;
import model.UserConstants;

public class ChatUnitHandler extends SimpleChannelInboundHandler<ChatUnitMessage> {

    private static final ConcurrentLinkedDeque<ChannelHandlerContext> clients = new ConcurrentLinkedDeque<>();

    private String name;
    private static int cnt = 0;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        clients.add(ctx);
        cnt++;
        name = "user#" + cnt;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ChatUnitMessage msg) throws Exception {

        if (msg.getSenderName().equals(UserConstants.DEFAULT_SENDER_NAME)) {
            msg.setSenderName(name);
        }

        for (ChannelHandlerContext client : clients) {
            client.writeAndFlush(msg);
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        clients.remove(ctx);
    }
}
