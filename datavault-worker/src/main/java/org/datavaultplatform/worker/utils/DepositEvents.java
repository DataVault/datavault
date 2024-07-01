package org.datavaultplatform.worker.utils;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.util.*;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;
import java.time.Instant;
import java.time.ZoneOffset;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.common.PropNames;
import org.datavaultplatform.common.event.Event;
import org.datavaultplatform.common.event.deposit.Complete;
import org.datavaultplatform.common.event.deposit.ComputedDigest;
import org.datavaultplatform.common.event.deposit.ComputedEncryption;
import org.datavaultplatform.common.event.deposit.UploadComplete;
import org.datavaultplatform.common.model.ArchiveStore;
import org.datavaultplatform.common.storage.StorageConstants;
import org.datavaultplatform.common.storage.Verify;
import org.datavaultplatform.worker.tasks.Audit;
import org.datavaultplatform.common.util.DateTimeUtils;
import org.datavaultplatform.worker.tasks.Deposit;
import org.datavaultplatform.worker.tasks.Retrieve;
import org.springframework.util.Assert;

@Slf4j
public class DepositEvents {

  public static final String FILE_STORE_SRC_ID = "FILE-STORE-SRC-ID";
  final List<Event> events;
  final Deposit deposit;

  public DepositEvents(Deposit deposit, List<Event> events) {
    this.deposit = deposit;
    this.events = new ArrayList<>(events);
  }

  @SuppressWarnings("UnnecessaryLocalVariable")
  @SneakyThrows
  public String generateRetrieveMessage(File retrieveBaseDir, String retrievePath) {

    TaskInfo info = getTaskInfo(retrieveBaseDir, retrievePath);

    Retrieve retrieve = new Retrieve();
    retrieve.setTaskClass("org.datavaultplatform.worker.tasks.Retrieve");
    Map<String, String> topLevelProps = new HashMap<>();
    topLevelProps.put(PropNames.ARCHIVE_DIGEST_ALGORITHM, Verify.SHA_1_ALGORITHM);
    topLevelProps.put(PropNames.BAG_ID, info.bagitId);
    topLevelProps.put(PropNames.NUM_OF_CHUNKS, String.valueOf(info.numChunks));
    topLevelProps.put(PropNames.RETRIEVE_PATH, retrievePath);
    topLevelProps.put(PropNames.ARCHIVE_SIZE, String.valueOf(info.archiveSize));
    topLevelProps.put(PropNames.ARCHIVE_DIGEST, info.archiveDigest);
    topLevelProps.put(PropNames.ARCHIVE_ID, info.archiveId);
    topLevelProps.put(PropNames.DEPOSIT_CREATION_DATE, "20240111");
    topLevelProps.put(PropNames.USER_FS_RETRY_MAX_ATTEMPTS, "10");
    topLevelProps.put(PropNames.USER_FS_RETRY_DELAY_MS_1, "60000");
    topLevelProps.put(PropNames.USER_FS_RETRY_DELAY_MS_2, "300000");
    Instant testInstant = LocalDate.of(2024, 1, 11)
            .atStartOfDay(ZoneOffset.UTC)
            .toInstant();
    Date testDate = Date.from(testInstant);
    topLevelProps.put(PropNames.DEPOSIT_CREATION_DATE, DateTimeUtils.formatDateBasicISO(testDate));

    retrieve.setProperties(topLevelProps);
    retrieve.setTarIV(info.tarIV);
    retrieve.setArchiveFileStores(List.of(getArchiveStoreForRetrieve()));
    //CHUNKS
    retrieve.setChunkFilesDigest(info.chunkDigests);
    retrieve.setEncChunksDigest(info.chunkEncDigests);
    retrieve.setChunksIVs(info.chunkIVsAsBytes);

    retrieve.setIsRedeliver(false);
    Map<String, String> userFileStoreClasses = new HashMap<>();

    Map<String,String> propsInner = new HashMap<>();
    propsInner.put(PropNames.ROOT_PATH, retrieveBaseDir.getCanonicalPath());
    Map<String, Map<String, String>> propsOuter = new HashMap<>();
    propsOuter.put(FILE_STORE_SRC_ID, propsInner);
    retrieve.setUserFileStoreProperties(propsOuter);
    userFileStoreClasses.put(FILE_STORE_SRC_ID, StorageConstants.LOCAL_FILE_SYSTEM);
    retrieve.setUserFileStoreClasses(userFileStoreClasses);

    ObjectMapper mapper = new ObjectMapper();
    mapper.setSerializationInclusion(Include.NON_NULL);
    String result = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(retrieve);
    return result;
  }

  /**
   * By using multiple DepositEvents - we can audit multiple deposits
   * @param allDepositEvents - information about the deposits to audit
   * @return the generated audit message.
   */

  @SneakyThrows
  public static String generateAuditMessage(List<DepositEvents> allDepositEvents) {

    Assert.isTrue(!allDepositEvents.isEmpty(), "must have at least 1 DepositEvents");

    DepositEvents depositEvents1 = allDepositEvents.get(0);

    Audit audit = new Audit();
    audit.setJobID("test-job-id");
    audit.setTaskClass("org.datavaultplatform.worker.tasks.Audit");

    Map<String, String> topLevelProps = new HashMap<>();
    topLevelProps.put(PropNames.AUDIT_ID, "test-audit-id");
    audit.setProperties(topLevelProps);
    audit.setArchiveFileStores(Collections.singletonList(depositEvents1.getArchiveStoreForRetrieve()));
    audit.setIsRedeliver(false);

    TaskInfo info = depositEvents1.getTaskInfo(null, null);
    audit.setTarIV(info.tarIV);

    List<ChunkInfo> allDepositChunkInfos = new ArrayList<>();
    for(DepositEvents depositEvents : allDepositEvents){
      TaskInfo taskInfo = depositEvents.getTaskInfo(null, null);
      List<ChunkInfo> depositChunkInfos = taskInfo.getChunkInfo();
      allDepositChunkInfos.addAll(depositChunkInfos);
    }

    int totalNumberOfChunks = allDepositChunkInfos.size();

    List<HashMap<String, String>> chunksToAudit = new ArrayList<>();
    String[] archiveIds = new String[totalNumberOfChunks];

    //CHUNKS
    Map<Integer, String> chunkDigests = new HashMap<>();
    Map<Integer, String> chunkEncDigests = new HashMap<>();
    Map<Integer, byte[]> chunkIVsAsBytes = new HashMap<>();

    for (int chunkIdx = 0; chunkIdx < totalNumberOfChunks; chunkIdx++) {
      ChunkInfo chunkInfo = allDepositChunkInfos.get(chunkIdx);
      HashMap<String, String> chunkProps = new HashMap<>();
      chunkProps.put(PropNames.CHUNK_NUM, String.valueOf(chunkInfo.chunkNum));
      chunkProps.put(PropNames.BAG_ID, chunkInfo.bagitId);
      chunkProps.put(PropNames.CHUNK_ID, String.format("db-id-for-bagid[%s]chunkNum[%s]",chunkInfo.bagitId,chunkInfo.chunkNum));
      chunksToAudit.add(chunkProps);

      chunkDigests.put(chunkIdx, chunkInfo.chunkDigest);
      chunkEncDigests.put(chunkIdx, chunkInfo.chunkEncDigest);
      chunkIVsAsBytes.put(chunkIdx, chunkInfo.chunkIVAsBytes);
      archiveIds[chunkIdx] = chunkInfo.getArchiveId();
    }
    audit.setChunksToAudit(chunksToAudit);
    audit.setArchiveIds(archiveIds);
    audit.setChunkFilesDigest(chunkDigests);
    audit.setEncChunksDigest(chunkEncDigests);
    audit.setChunksIVs(chunkIVsAsBytes);

    ObjectMapper mapper = new ObjectMapper();
    mapper.setSerializationInclusion(Include.NON_NULL);
    String result = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(audit);
    System.out.println(result);


    return result;
  }


  @SuppressWarnings("UnnecessaryLocalVariable")
  public ArchiveStore getArchiveStoreForRetrieve() {
    ArchiveStore result = deposit.getArchiveFileStores().stream()
        .filter(ArchiveStore::isRetrieveEnabled)
        .findFirst()
        .orElse(null);
    return result;
  }

  @SuppressWarnings("unchecked")
  private <T> T findEvent(Class<T> clazz) {
    return events.stream()
        .filter(e -> clazz.isAssignableFrom(e.getClass()))
        .map(e -> (T) e)
        .findFirst()
        .orElse(null);
  }


  public ComputedEncryption getComputedEncryption() {
    return findEvent(ComputedEncryption.class);
  }

  public UploadComplete getUploadComplete() {
    return findEvent(UploadComplete.class);
  }

  public Complete getComplete() {
    return findEvent(Complete.class);
  }

  public ComputedDigest getComputedDigest() {
    return findEvent(ComputedDigest.class);
  }

  @SuppressWarnings("UnnecessaryLocalVariable")
  @SneakyThrows
  public TaskInfo getTaskInfo(File retrieveBaseDir, String retrievePath) {

    String bagitId = deposit.getProperties().get(PropNames.BAG_ID);

    ComputedEncryption computedEncryption = getComputedEncryption();
    HashMap<Integer, byte[]> tempChunkIVS = computedEncryption.getChunkIVs();
    int numChunks = tempChunkIVS == null ? 0  : tempChunkIVS.size();

    long archiveSize = getComplete().getArchiveSize();

    String archiveDigest = getComputedDigest().getDigest();

    String archiveId = getUploadComplete().getArchiveIds().get(getArchiveStoreForRetrieve().getID());

    Map<Integer,String> chunkIVs = new HashMap<>();
    Map<Integer,byte[]> chunkIVsAsBytes = new HashMap<>();
    Map<Integer,String> chunkDigests = new HashMap<>();
    Map<Integer,String> chunkEncDigests = new HashMap<>();

    byte[] tarIV = numChunks == 0 ? computedEncryption.getTarIV() : null;
    for (int i = 0; i < numChunks; i++) {
      int chunkNumber = i + 1;

      chunkIVs.put(chunkNumber,
          base64Encode(computedEncryption.getChunkIVs().get(chunkNumber)));

      chunkIVsAsBytes.put(chunkNumber,
          computedEncryption.getChunkIVs().get(chunkNumber));

      chunkDigests.put(chunkNumber,
          computedEncryption.getChunksDigest().get(chunkNumber));

      chunkEncDigests.put(chunkNumber,
          computedEncryption.getEncChunkDigests().get(chunkNumber));
    }

    String rootPathArchiveStore = getArchiveStoreForRetrieve().getProperties().get(PropNames.ROOT_PATH);
    String rootPathRetrieve = null;
    if (retrieveBaseDir != null) {
      rootPathRetrieve = retrieveBaseDir.getCanonicalPath();
    }
    TaskInfo info = TaskInfo.builder()
            .bagitId(bagitId)
            .numChunks(numChunks)
            .archiveSize(archiveSize)
            .archiveId(archiveId)
            .rootPathArchiveStore(rootPathArchiveStore)
            .rootPathRetrieve(rootPathRetrieve)
            .retrievePath(retrievePath)
            .tarIV(tarIV)
            .chunkDigests(chunkDigests)
            .chunkEncDigests(chunkEncDigests)
            .chunkIVs(chunkIVs)
            .chunkIVsAsBytes(chunkIVsAsBytes)
            .build();
    return info;
  }


  @Data
  @Builder
  public static class TaskInfo {

    private final String bagitId;
    private final int numChunks;

    private final long archiveSize;
    private final String archiveDigest;
    private final String archiveId;

    private final Map<Integer,String> chunkDigests;

    private final Map<Integer,String> chunkEncDigests;
    private final Map<Integer,String> chunkIVs;
    private final Map<Integer,byte[]> chunkIVsAsBytes;

    private final String rootPathArchiveStore;
    private final String rootPathRetrieve;
    private final String retrievePath;

    private final byte[] tarIV;

    public List<ChunkInfo> getChunkInfo() {
      List<ChunkInfo> result = new ArrayList<>();
      Set<Integer> keys = this.chunkDigests.keySet();
      for(Integer key : keys){
        String chunkDigest = this.chunkDigests.get(key);
        String chunkEncDigest = this.chunkEncDigests.get(key);
        String chunkIV = this.chunkIVs.get(key);
        byte[] chunkIVBytes = this.chunkIVsAsBytes.get(key);

        ChunkInfo chunkInfo = ChunkInfo
                .builder()
                .bagitId(bagitId)
                .archiveId(archiveId)
                .chunkNum(key)
                .chunkDigest(chunkDigest)
                .chunkEncDigest(chunkEncDigest)
                .chunkIVAsString(chunkIV)
                .chunkIVAsBytes(chunkIVBytes)
                .build();
        result.add(chunkInfo);
      }
      Collections.sort(result);
      return result;
    }

    private <T> HashMap<Integer,T> zeroBased(Map<Integer,T> source){
      HashMap<Integer,T> result = new HashMap<>();

      source.entrySet().forEach(entry -> {
        Integer key = entry.getKey();
        T value = entry.getValue();
        result.put(key - 1, value);
      });

      return result;
    }

  }

  private static String base64Encode(byte[] data) {
    return Base64.getEncoder().encodeToString(data);
  }

  @Data
  @Builder
  public static class ChunkInfo implements Comparable<ChunkInfo> {
    private final int chunkNum;
    private final String bagitId;
    private final String archiveId;
    private final String chunkDigest;
    private final String chunkEncDigest;
    private final String chunkIVAsString;
    private final byte[] chunkIVAsBytes;

    @Override
    public int compareTo(DepositEvents.ChunkInfo chunkInfo) {
      return Integer.compare(this.chunkNum, chunkInfo.chunkNum);
    }
  }
}
