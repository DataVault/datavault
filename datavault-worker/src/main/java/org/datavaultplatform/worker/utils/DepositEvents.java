package org.datavaultplatform.worker.utils;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.common.event.Event;
import org.datavaultplatform.common.event.deposit.Complete;
import org.datavaultplatform.common.event.deposit.ComputedDigest;
import org.datavaultplatform.common.event.deposit.ComputedEncryption;
import org.datavaultplatform.common.event.deposit.UploadComplete;
import org.datavaultplatform.common.model.ArchiveStore;
import org.datavaultplatform.common.storage.Verify;
import org.datavaultplatform.common.PropNames;
import org.datavaultplatform.worker.tasks.Deposit;
import org.datavaultplatform.worker.tasks.Retrieve;

@Slf4j
public class DepositEvents {

  public static final String FILE_STORE_SRC_ID = "FILE-STORE-SRC-ID";
  final List<Event> events;
  final Deposit deposit;

  public DepositEvents(Deposit deposit, List<Event> events) {
    this.deposit = deposit;
    this.events = events;
  }

  @SneakyThrows
  public String generateRetrieveMessage(File retrieveBaseDir, String retrievePath) {

    RetrieveInfo info = getRetrieveInfo(retrieveBaseDir, retrievePath);

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

    retrieve.setProperties(topLevelProps);

    retrieve.setArchiveFileStores(Collections.singletonList(getArchiveStoreForRetrieve()));
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
    userFileStoreClasses.put(FILE_STORE_SRC_ID, "org.datavaultplatform.common.storage.impl.LocalFileSystem");
    retrieve.setUserFileStoreClasses(userFileStoreClasses);

    ObjectMapper mapper = new ObjectMapper();
    mapper.setSerializationInclusion(Include.NON_NULL);
    String result = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(retrieve);
    return result;
  }

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

  @SneakyThrows
  public RetrieveInfo getRetrieveInfo(File retrieveBaseDir, String retrievePath) {
    RetrieveInfo info = new RetrieveInfo();
    info.bagitId = deposit.getProperties().get(PropNames.BAG_ID);

    ComputedEncryption computedEncryption = getComputedEncryption();
    info.numChunks = computedEncryption.getChunkIVs().size();

    //info.retrievePath = this.retrieveDir.getName();
    info.retrievePath = retrievePath;

    info.archiveSize = getComplete().getArchiveSize();

    info.archiveDigest = getComputedDigest().getDigest();

    info.archiveId = getUploadComplete().getArchiveIds().get(getArchiveStoreForRetrieve().getID());

    info.chunkIVs = new HashMap<>();
    info.chunkIVsAsBytes = new HashMap<>();
    info.chunkDigests = new HashMap<>();
    info.chunkEncDigests = new HashMap<>();

    for (int i = 0; i < info.numChunks; i++) {
      int chunkNumber = i + 1;

      info.chunkIVs.put(chunkNumber,
          base64Encode(computedEncryption.getChunkIVs().get(chunkNumber)));

      info.chunkIVsAsBytes.put(chunkNumber,
          computedEncryption.getChunkIVs().get(chunkNumber));

      info.chunkDigests.put(chunkNumber,
          computedEncryption.getChunksDigest().get(chunkNumber));

      info.chunkEncDigests.put(chunkNumber,
          computedEncryption.getEncChunkDigests().get(chunkNumber));
    }

    //info.rootPathArchiveStore = this.destDir.getCanonicalPath();
    info.rootPathArchiveStore = getArchiveStoreForRetrieve().getProperties().get(PropNames.ROOT_PATH);
    //info.rootPathRetrieve = this.retrieveBaseDir.getCanonicalPath();
    info.rootPathRetrieve = retrieveBaseDir.getCanonicalPath();

    return info;
  }


  public static class RetrieveInfo {

    public String bagitId;
    public int numChunks;

    public long archiveSize;
    public String archiveDigest;
    public String archiveId;

    public Map<Integer,String> chunkDigests;

    public Map<Integer,String> chunkEncDigests;
    public Map<Integer,String> chunkIVs;
    public Map<Integer,byte[]> chunkIVsAsBytes;

    public String rootPathArchiveStore;
    public String rootPathRetrieve;
    public String retrievePath;
  }

  private static String base64Encode(byte[] data) {
    return Base64.getEncoder().encodeToString(data);
  }


}
