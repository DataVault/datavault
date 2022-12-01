package org.datavaultplatform.common.io;

/**
 * The ProgressEventListener was introduced to help test Progress changes during SFTP file transfers.
 */
public class Progress {
    private long startTime = 0;
    private long dirCount = 0;
    private long fileCount = 0;
    private long byteCount = 0;
    private long timestamp = 0;

    private final ProgressEventListener listener;

    public Progress() {
        this(null);
    }

    public Progress(ProgressEventListener listener) {
        super();
        this.listener = listener;
    }

    private void publish(ProgressEventType eventType, long value) {
        if (listener != null) {
            listener.onEvent(ProgressEvent.builder().type(eventType).value(value).build());
        }
    }

    public synchronized void incByteCount(long inc) {
        this.byteCount += inc;
        publish(ProgressEventType.BYTE_COUNT_INC, this.byteCount);
    }

    public synchronized void incFileCount(int inc) {
        this.fileCount += inc;
        publish(ProgressEventType.FILE_COUNT_INC, this.fileCount);
    }

    public synchronized void incDirCount(int inc) {
        this.dirCount += inc;
        publish(ProgressEventType.DIR_COUNT_INC, this.dirCount);
    }

    public synchronized long getStartTime() {
        return startTime;
    }

    public synchronized void setStartTime(long startTime) {
        this.startTime = startTime;
        publish(ProgressEventType.START_TIME_SET, this.startTime);
    }

    public synchronized long getDirCount() {
        return dirCount;
    }

    public synchronized void setDirCount(long dirCount) {
        this.dirCount = dirCount;
        publish(ProgressEventType.DIR_COUNT_SET, this.dirCount);
    }

    public synchronized long getFileCount() {
        return fileCount;
    }

    public synchronized void setFileCount(long fileCount) {
        this.fileCount = fileCount;
        publish(ProgressEventType.FILE_COUNT_SET, this.fileCount);
    }

    public synchronized long getByteCount() {
        return byteCount;
    }

    public synchronized void setByteCount(long byteCount) {
        this.byteCount = byteCount;
        publish(ProgressEventType.BYTE_COUNT_SET, this.byteCount);
    }

    public synchronized long getTimestamp() {
        return timestamp;
    }

    public synchronized void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
        publish(ProgressEventType.TIMESTAMP_SET, this.timestamp);
    }
}
