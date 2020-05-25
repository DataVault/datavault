package org.datavaultplatform.worker.tasks;

public class EncryptionHelper {
	private byte[] iv;
	private String encTarHash;
	private int chunkCount;
	
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

	public int getChunkCount() {
		return this.chunkCount;
	}

	public void setChunkCount(int chunkCount) {
		this.chunkCount = chunkCount;
	}
}
