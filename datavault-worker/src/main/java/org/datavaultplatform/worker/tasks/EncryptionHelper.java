package org.datavaultplatform.worker.tasks;

public class EncryptionHelper {
	private byte[] iv;
	private String encTarHash;
	
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
}
