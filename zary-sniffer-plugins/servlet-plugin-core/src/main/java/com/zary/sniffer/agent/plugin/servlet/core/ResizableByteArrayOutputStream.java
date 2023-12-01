package com.zary.sniffer.agent.plugin.servlet.core;

import java.io.ByteArrayOutputStream;

public class ResizableByteArrayOutputStream extends ByteArrayOutputStream {
    private static final int DEFAULT_INITIAL_CAPACITY = 256;

    public ResizableByteArrayOutputStream() {
        super(256);
    }

    public ResizableByteArrayOutputStream(int initialCapacity) {
        super(initialCapacity);
    }

    public synchronized void resize(int targetCapacity) {
        //Assert.isTrue(targetCapacity >= this.count, "New capacity must not be smaller than current size");
        if (targetCapacity < this.count)
            throw new IllegalArgumentException("New capacity must not be smaller than current size");

        byte[] resizedBuffer = new byte[targetCapacity];
        System.arraycopy(this.buf, 0, resizedBuffer, 0, this.count);
        this.buf = resizedBuffer;
    }

    public synchronized void grow(int additionalCapacity) {
        //Assert.isTrue(additionalCapacity >= 0, "Additional capacity must be 0 or higher");
        if (additionalCapacity < 0) {
            throw new IllegalArgumentException("Additional capacity must be 0 or higher");
        }

        if (this.count + additionalCapacity > this.buf.length) {
            int newCapacity = Math.max(this.buf.length * 2, this.count + additionalCapacity);
            this.resize(newCapacity);
        }

    }

    public synchronized int capacity() {
        return this.buf.length;
    }
}
