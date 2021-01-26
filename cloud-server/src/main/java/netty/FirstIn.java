package netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FirstIn extends ChannelInboundHandlerAdapter {

    private static final Logger LOG = LoggerFactory.getLogger(FirstIn.class);

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        LOG.debug("Client accepted!");
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = (ByteBuf) msg;
        LOG.debug("received message: {}", buf);
        StringBuilder s = new StringBuilder();
        while (buf.isReadable()) {
            s.append((char) buf.readByte());
        }
        String message = s.toString();
        LOG.debug("converted to string: {}", message);
        ctx.fireChannelRead(message);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        LOG.debug("Client disconnected!");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        LOG.error("e=", cause);
    }
}
