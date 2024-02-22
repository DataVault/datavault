package org.datavaultplatform.worker.tasks;
import java.io.File;

public record ChecksumHelper(int chunkNumber, String chunkHash, File chunk) {

}
