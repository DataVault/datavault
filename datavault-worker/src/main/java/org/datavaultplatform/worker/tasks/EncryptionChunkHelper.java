package org.datavaultplatform.worker.tasks;

public class EncryptionChunkHelper extends EncryptionHelper {
	private final int chunkNumber;

	public EncryptionChunkHelper(byte[] iv, String encTarHash, int chunkNumber) {
		super(iv, encTarHash);
		this.chunkNumber = chunkNumber;
	}
	public int getChunkNumber() {
		return this.chunkNumber;
	}
}
