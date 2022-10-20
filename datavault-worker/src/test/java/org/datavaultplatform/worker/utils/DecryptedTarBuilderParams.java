package org.datavaultplatform.worker.utils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder({
    "keystorePath", "keystorePassword",
    "dataKeyName", "dataKeyDigest",
    "tarDir", "tarChecksum",
    "chunksDir", "chunkData"
})
public class DecryptedTarBuilderParams {

  private final Map<Integer, ChunkData> chunkData = new HashMap<>();
  private String keystorePath;
  private String keystorePassword;
  private String dataKeyName;

  @JsonInclude(Include.NON_NULL)
  private String dataKeyDigest;
  private String tarDir;
  @JsonInclude(Include.NON_NULL)
  private String tarChecksum;
  private String chunksDir;

  public void setChunkData(Map<Integer, ChunkData> data) {
    this.chunkData.clear();
    this.chunkData.putAll(data);
  }
  public Map<Integer, ChunkData> getChunkData() {
    return Collections.unmodifiableMap(this.chunkData);
  }

  @Data
  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  @JsonIgnoreProperties(ignoreUnknown = true)
  @JsonPropertyOrder({
      "iv", "ivBytes", "ivDigest",
      "encryptedChecksum", "decryptedChecksum"
  })
  public static class ChunkData {

    @JsonInclude(Include.NON_NULL)
    private String iv;

    @JsonIgnore
    private byte[] ivBytes;

    @JsonInclude(Include.NON_NULL)
    private String ivDigest;

    @JsonInclude(Include.NON_NULL)
    private String encryptedChecksum;

    @JsonInclude(Include.NON_NULL)
    private String decryptedChecksum;

    public ChunkData withIvString() {
      ChunkData result = new ChunkData();
      result.setDecryptedChecksum(this.decryptedChecksum);
      result.setEncryptedChecksum(this.encryptedChecksum);
      result.setIvDigest(this.ivDigest);
      String ivString = StringUtils.isNotBlank(iv) ? iv
          : java.util.Base64.getEncoder().encodeToString(this.ivBytes);
      result.setIv(ivString);
      result.setIvBytes(null);
      return result;
    }
  }
}
