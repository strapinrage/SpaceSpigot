// From
// https://github.com/Argarian-Network/NachoSpigot/tree/async-kb-hit
package net.strapinrage.spacespigot.async.thread;

import io.netty.channel.Channel;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import net.strapinrage.spacespigot.async.netty.Spigot404Write;
import net.strapinrage.spacespigot.config.SpaceSpigotConfig;
import net.minecraft.server.NetworkManager;
import net.minecraft.server.Packet;

public abstract class AsyncPacketThread {
    private boolean running = true;
	private static final long SEC_IN_NANO = 1000000000;
	private static final int TPS = SpaceSpigotConfig.combatThreadTPS;
	private static final long TICK_TIME = SEC_IN_NANO / TPS;
	private static final long MAX_CATCHUP_BUFFER = TICK_TIME * TPS * 60L;
    private Thread thread;
    protected Queue<Runnable> packets = new ConcurrentLinkedQueue<Runnable>();

    public AsyncPacketThread(String s) {
        this.thread = new Thread(new Runnable() {

            @Override
            public void run() {
            	AsyncPacketThread.this.loop();
            }
        }, s);
        this.thread.start();
    }
    
    public void loop() {

		long lastTick = System.nanoTime();
		long catchupTime = 0L;

		while (this.running) {
			long curTime = System.nanoTime();
			long wait = TICK_TIME - (curTime - lastTick);

			if (wait > 0) {
				if (catchupTime < 2E6) {
					wait += Math.abs(catchupTime);
				} else if (wait < catchupTime) {
					wait = 0;
				} else {
					wait -= catchupTime;
				}

				try {
					Thread.sleep(wait / 1000000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				catchupTime = 0L;
				continue;
			}

			catchupTime = Math.min(MAX_CATCHUP_BUFFER, catchupTime - wait);

			this.run();
			lastTick = curTime;
		}
	}

    public abstract void run();

    public void addPacket(final Packet<?>  packet, final NetworkManager manager, final GenericFutureListener<? extends Future<? super Void>>[] agenericfuturelistener) {
        this.packets.add(new Runnable() {

            @Override
            public void run() {
                Spigot404Write.writeThenFlush(manager.channel, packet, agenericfuturelistener);
            }
        });
    }

    public Thread getThread() {
        return this.thread;
    }

    public static class RunnableItem {
        private Channel channel;
        private Packet<?>  packet;

        public RunnableItem(Channel m, Packet<?>  p) {
            this.channel = m;
            this.packet = p;
        }

        public Packet<?> getPacket() {
            return this.packet;
        }

        public Channel getChannel() {
            return this.channel;
        }
    }
} 