package com.facebook.nifty.core;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.DownstreamMessageEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.jboss.netty.channel.UpstreamMessageEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Counters for number of channels open, generic traffic stats and maybe cleanup logic here.
 *
 * @author jaxlaw
 */
public class ChannelStatistics extends SimpleChannelHandler {
    private static final Logger log = LoggerFactory.getLogger(ChannelStatistics.class);

    // TODO : add channel stats here
    private static final AtomicInteger channelCount = new AtomicInteger(0);
    private final AtomicLong bytesRead = new AtomicLong(0);
    private final AtomicLong bytesWritten = new AtomicLong(0);

    public static final String NAME = ChannelStatistics.class.getSimpleName();

    public void handleUpstream(ChannelHandlerContext ctx, ChannelEvent e) throws Exception {
        if (e instanceof ChannelStateEvent) {
            ChannelStateEvent cse = (ChannelStateEvent) e;
            switch (cse.getState()) {
                case OPEN:
                    if (Boolean.TRUE.equals(cse.getValue())) {
                        // connect
                        channelCount.incrementAndGet();
                    } else {
                        // disconnect
                        channelCount.decrementAndGet();
                    }
                    break;
                case BOUND:
                    break;
            }
        }

        if (e instanceof UpstreamMessageEvent) {
            UpstreamMessageEvent ume = (UpstreamMessageEvent) e;
            if (ume.getMessage() instanceof ChannelBuffer) {
                ChannelBuffer cb = (ChannelBuffer) ume.getMessage();
                int readableBytes = cb.readableBytes();
                //  compute stats here, bytes read from remote
                bytesRead.getAndAdd(readableBytes);
            }
        }

        ctx.sendUpstream(e);
    }

    public void handleDownstream(ChannelHandlerContext ctx, ChannelEvent e) throws Exception {
        if (e instanceof DownstreamMessageEvent) {
            DownstreamMessageEvent dme = (DownstreamMessageEvent) e;
            if (dme.getMessage() instanceof ChannelBuffer) {
                ChannelBuffer cb = (ChannelBuffer) dme.getMessage();
                int readableBytes = cb.readableBytes();
                // compute stats here, bytes written to remote
                bytesWritten.getAndAdd(readableBytes);
            }
        }
        ctx.sendDownstream(e);
    }

    public static int getChannelCount() {
        return channelCount.get();
    }

    public long getBytesRead() {
        return bytesRead.get();
    }

    public long getBytesWritten() {
        return bytesWritten.get();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
    }
}


