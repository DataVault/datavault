package org.datavaultplatform.worker.utils;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.NumberSerializers.IntegerSerializer;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class DecryptedTarBuilderParams {

  private String chunksDir;
  private String tarDir;

  private String keystorePath;
  private String keystorePassword;
  private String dataKeyName;
  private String dataKeyDigest;

  @JsonSerialize(keyUsing = IntegerSerializer.class)
  private Map<Integer,ChunkData> chunkData;
  private String tarChecksum;

  @Data
  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  @JsonIgnoreProperties(ignoreUnknown = true)
  static class ChunkData {
    private String iv;
    private String ivDigest;
    private String encryptedChecksum;
    private String decryptedChecksum;
  }

}
