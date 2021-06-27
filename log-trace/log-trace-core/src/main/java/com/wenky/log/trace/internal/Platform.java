package com.wenky.log.trace.internal;


import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.security.SecureRandom;
import java.util.Enumeration;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * @author zhongwenjian
 * @date 2021/6/25
 */
public abstract class Platform {
    private static final Platform PLATFORM = findPlatform();

    volatile String linkLocalIp;

    /** Guards {@link InetSocketAddress#getHostString()}, as it isn't available until Java 7 */
    public abstract String getHostString(InetSocketAddress socket);

    public String linkLocalIp() {
        // uses synchronized variant of double-checked locking as getting the endpoint can be expensive
        if (linkLocalIp != null) {
            return linkLocalIp;
        }
        synchronized (this) {
            if (linkLocalIp == null) {
                linkLocalIp = produceLinkLocalIp();
            }
        }
        return linkLocalIp;
    }

    String produceLinkLocalIp() {
        try {
            Enumeration<NetworkInterface> nics = NetworkInterface.getNetworkInterfaces();
            while (nics.hasMoreElements()) {
                NetworkInterface nic = nics.nextElement();
                Enumeration<InetAddress> addresses = nic.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress address = addresses.nextElement();
                    if (address.isSiteLocalAddress()) {
                        return address.getHostAddress();
                    }
                }
            }
        } catch (Exception e) {
            // don't crash the caller if there was a problem reading nics.
            log("error reading nics", e);
        }
        return null;
    }

    public static Platform get() {
        return PLATFORM;
    }

    /** Use nested class to ensure logger isn't initialized unless it is accessed once.*/
    private static final class LoggerHolder {
        static final Logger LOG = Logger.getLogger(Platform.class.getName());
    }

    /** Like {@link Logger#log(Level, String) */
    public void log(String msg, Throwable thrown) {
        Logger logger = LoggerHolder.LOG;
        if (!logger.isLoggable(Level.FINE)) {
            return;
        }
        logger.log(Level.FINE, msg, thrown);
    }

    /** Like {@link Logger#log(Level, String, Object)}, except with a throwable arg */
    public void log(String msg, Object param1, Throwable thrown) {
        Logger logger = LoggerHolder.LOG;
        if (!logger.isLoggable(Level.FINE)) {
            return;
        }
        LogRecord lr = new LogRecord(Level.FINE, msg);
        Object[] params = {param1};
        lr.setParameters(params);
        if (thrown != null) {
            lr.setThrown(thrown);
        }
        logger.log(lr);
    }

    /** Attempt to match the host runtime to a capable Platform implementation. */
    static Platform findPlatform() {
        // Find JRE 9 new methods
        try {
            Class zoneId = Class.forName("java.time.ZoneId");
            Class.forName("java.time.Clock").getMethod("tickMillis", zoneId);
            return new Jre9(); // intentionally doesn't not access the type prior to the above guard
        } catch (ClassNotFoundException e) {
            // pre JRE 8
        } catch (NoSuchMethodException e) {
            // pre JRE 9
        }

        // Find JRE 7 new methods
        try {
            Class.forName("java.util.concurrent.ThreadLocalRandom");
            return new Jre7(); // intentionally doesn't not access the type prior to the above guard
        } catch (ClassNotFoundException e) {
            // pre JRE 7
        }

        // compatible with JRE 6
        return new Jre6();
    }

    /**
     * This class uses pseudo-random number generators to provision IDs.
     *
     * <p>This optimizes speed over full coverage of 64-bits, which is why it doesn't share a {@link
     * SecureRandom}. It will use {@link java.util.concurrent.ThreadLocalRandom} unless used in JRE 6
     * which doesn't have the class.
     *
     * @return a random long number
     */
    public abstract long randomLong();

    /**
     *
     * <p>The upper 4-bytes are epoch seconds and the lower 4-bytes are random. This makes it
     * convertible to <a href="http://docs.aws.amazon.com/elasticloadbalancing/latest/application/load-balancer-request-tracing.html"></a>Amazon
     * X-Ray trace ID format v1</a>.
     *
     * @return the high trace id
     */
    public abstract long nextTraceIdHigh();

    static class Jre9 extends Jre7 {

        @Override public String toString() {
            return "Jre9{}";
        }
    }

    static class Jre7 extends Platform {
        @Override
        public String getHostString(InetSocketAddress socket) {
            return socket.getHostString();
        }

        @Override public long randomLong() {
            return java.util.concurrent.ThreadLocalRandom.current().nextLong();
        }

        @Override public long nextTraceIdHigh() {
            return nextTraceIdHigh(java.util.concurrent.ThreadLocalRandom.current().nextInt());
        }

        @Override public String toString() {
            return "Jre7{}";
        }
    }

    static long nextTraceIdHigh(int random) {
        long epochSeconds = System.currentTimeMillis() / 1000;
        return (epochSeconds & 0xffffffffL) << 32
                | (random & 0xffffffffL);
    }

    static class Jre6 extends Platform {

        @Override public String getHostString(InetSocketAddress socket) {
            return socket.getAddress().getHostAddress();
        }

        @Override public long randomLong() {
            return prng.nextLong();
        }

        @Override public long nextTraceIdHigh() {
            return nextTraceIdHigh(prng.nextInt());
        }

        final Random prng;

        Jre6() {
            this.prng = new Random(System.nanoTime());
        }

        @Override public String toString() {
            return "Jre6{}";
        }
    }
}
