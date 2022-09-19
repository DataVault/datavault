package org.datavaultplatform.worker.tasks;

public class EncryptionHelper {
	private byte[] iv;
	private String encTarHash;
	private int chunkNumber;
	
	public byte[] getIv() {
		return this.iv;
	}
	public void setIv(byte[] iv) {
		this.iv = iv;
	}
	
	public String getEncTarHash() {
		return this.encTarHash;
	}
	public void setEncTarHash(String encTarHash) {
		this.encTarHash = encTarHash;
	}

	public int getChunkNumber() {
		return this.chunkNumber;
	}

	public void setChunkNumber(int chunkNumber) {
		this.chunkNumber = chunkNumber;
	}
}
