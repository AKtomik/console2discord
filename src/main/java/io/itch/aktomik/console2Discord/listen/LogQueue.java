package io.itch.aktomik.console2Discord.listen;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public final class LogQueue {

    private final LinkedBlockingQueue<LogUnit> queue = new LinkedBlockingQueue<>();
    private volatile int capacity;

    public LogQueue(int capacity) {
        this.capacity = capacity;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public boolean offer(LogUnit unit) {
        if (queue.size() >= capacity) return false;
        return queue.offer(unit);
    }

    public LogUnit poll(long timeout, TimeUnit unit) throws InterruptedException {
        return queue.poll(timeout, unit);
    }

    public LogUnit poll() {
        return queue.poll();
    }

    public boolean isEmpty() {
        return queue.isEmpty();
    }
}