package org.datavaultplatform.common.task;

public record ContextVaultInfo(String vaultAddress, String vaultToken,
                               String vaultKeyPath, String vaultKeyName,
                               String vaultSslPEMPath) {
}
