package org.datavaultplatform.worker.tasks;

public class EncryptionHelper {
	private final byte[] iv;
	private final String encTarHash;

	public EncryptionHelper(byte[] iv, String encTarHash) {
		this.iv = iv;
		this.encTarHash = encTarHash;
	}

	public byte[] getIv() {
		return this.iv;
	}
	public String getEncTarHash() {
		return this.encTarHash;
	}
}
