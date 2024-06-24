package org.datavaultplatform.worker.tasks;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.datavaultplatform.common.PropNames;
import org.datavaultplatform.common.task.Context;
import org.datavaultplatform.worker.tasks.deposit.DepositUtils;
import org.springframework.util.Assert;

import java.io.File;
import java.io.Serializable;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@SuppressWarnings("CodeBlock2Expr")
@Getter
@Setter
@Slf4j
public class PackageHelper implements Serializable {

    private File tarFile;
    private long archiveSize;

    private String tarHash;
    private byte[] iv;
    private String encTarHash;
    
    private boolean chunked;
    private boolean encrypted;

    private final Map<Integer, ChunkHelper> chunkHelpers = new HashMap<>();

    public ChunkHelper getChunkHelper(int chunkNumber) {
        return this.chunkHelpers.computeIfAbsent(chunkNumber, ChunkHelper::new);
    }

    public void addChunkHelper(ChunkHelper chunkHelper) {
        this.chunkHelpers.put(chunkHelper.getChunkNumber(), chunkHelper);
    }

    @Getter
    @Setter
    public static class ChunkHelper implements Serializable {
        private final int chunkNumber;
        private boolean encrypted;
        private File chunkFile;
        private String chunkHash;
        private byte[] chunkIV;
        private String chunkEncHash;

        public ChunkHelper(int chunkNumber) {
            this.chunkNumber = chunkNumber;
        }
    }
    public HashMap<Integer,String> getChunkHashes() {
        return getChunkMap(ChunkHelper::getChunkHash);
    }
    public HashMap<Integer,String> getEncChunkHashes() {
        return getChunkMap(ChunkHelper::getChunkEncHash);
    }
    public HashMap<Integer,byte[]> getChunksIVs() {
        return getChunkMap(ChunkHelper::getChunkIV);
    }
    private <T> HashMap<Integer,T> getChunkMap(Function<ChunkHelper,T> extractor){
        HashMap<Integer, T> result = new HashMap<>();
        chunkHelpers.forEach((chunkNumber, chunkHelper) -> {
            result.put(chunkNumber, extractor.apply(chunkHelper));
        });
        return result;
    }
    
    public File[] getChunkFiles() {
        File[] result = new File[this.getChunkHelpers().size()];
        chunkHelpers.forEach((chunkNumber, chunkHelper) -> {
            result[chunkNumber-1]=chunkHelper.getChunkFile();
        });
        return result;
    }
    public String[] getChunksHash() {
        String[] result = new String[this.getChunkHelpers().size()];
        chunkHelpers.forEach((chunkNumber, chunkHelper) -> {
            result[chunkNumber-1]=chunkHelper.getChunkHash();
        });
        return result;
    }
    public String[] getEncChunksHash() {
        String[] result = new String[this.getChunkHelpers().size()];
        chunkHelpers.forEach((chunkNumber, chunkHelper) -> {
            result[chunkNumber-1]=chunkHelper.getChunkEncHash();
        });
        return result;
    }
    
    public List<File> getPackagedFiles() {
        List<File> temp = new ArrayList<>();
        this.chunkHelpers.forEach((chunkNum, chunkHelper)->{
            temp.add(chunkHelper.chunkFile);
        });
        if(temp.isEmpty()){
            temp.add(this.tarFile);
        }
        return temp.stream().filter(Objects::nonNull).toList();
    }

    public static PackageHelper constructFromDepositTask(String bagID, Context context, Deposit deposit) {
        var result = new PackageHelper();

        result.setChunked(context.isChunkingEnabled());
        result.setEncrypted(context.isEncryptionEnabled());

        result.setTarFile(DepositUtils.getTarFile(context, bagID));
        result.setTarHash(deposit.getProperties().get(PropNames.ARCHIVE_DIGEST));

        int numberOfChunks = DepositUtils.getNumberOfChunks(deposit.getProperties());

        if (numberOfChunks == 0) {
            result.setIv(deposit.getTarIV());
            result.setEncTarHash(deposit.getEncTarDigest());
        } else {
            
            Map<Integer, String> chunkFilesDigest = safeGetMap(deposit::getChunkFilesDigest);
            Map<Integer, String> encChunkFilesDigest = safeGetMap(deposit::getEncChunksDigest);
            Map<Integer, byte[]> chunkIVs = safeGetMap(deposit::getChunksIVs);
            
            validateChunkMap("chunkFilesDigest", numberOfChunks, chunkFilesDigest);
            validateChunkMap("encChunkFilesDigest", numberOfChunks, encChunkFilesDigest);
            validateChunkMap("chunkIVs", numberOfChunks, chunkIVs);

            for (int chunkNumber = 1; chunkNumber <= numberOfChunks; chunkNumber++) {

                ChunkHelper helper = result.getChunkHelper(numberOfChunks);
                helper.setChunkFile(DepositUtils.getChunkTarFile(context, bagID, chunkNumber));
                helper.setChunkHash(chunkFilesDigest.get(chunkNumber));
                
                // for encrypted chunks
                helper.setChunkIV(chunkIVs.get(chunkNumber));
                helper.setChunkEncHash(encChunkFilesDigest.get(chunkNumber));
            }
        }
        result.validatePackagedFilesExist();
        return result;
    }

    /*
    Just to validate that the chunk maps have the expected number of entries
     */
    protected static <T> boolean validateChunkMap(String label, int numberOfChunks, Map<Integer, T> chunkMap) {
        Assert.isTrue(numberOfChunks >= 0, "The number of chunks cannot be negative");
        Assert.isTrue(chunkMap != null, "The chunkMap cannot be null");

        Set<Integer> expectedKeys = switch (numberOfChunks) {
            case 0 -> Collections.emptySet();
            default -> IntStream.rangeClosed(1, numberOfChunks).boxed().collect(Collectors.toSet());
        };
        Set<Integer> actualKeys = chunkMap.keySet();
        if(expectedKeys.equals(actualKeys)){
            return true;
        }
        Set<Integer> missingKeys = new TreeSet<>(CollectionUtils.removeAll(expectedKeys, actualKeys));
        log.warn("for map[{}], [{}] missing keys {}", label, missingKeys.size(), missingKeys);

        Set<Integer> extraKeys = new TreeSet<>(CollectionUtils.removeAll(actualKeys, expectedKeys));
        log.warn("for map[{}], [{}] extra keys {}", label, extraKeys.size(), extraKeys);
        return false;
    }

    private static <T> Map<Integer, T> safeGetMap(Supplier<Map<Integer, T>> mapSupplier) {
        Map<Integer, T> result = mapSupplier.get();
        return Objects.requireNonNullElse(result, Collections.emptyMap());
    }
    
    public boolean validatePackagedFilesExist() {
        Stream.Builder<File> builder = Stream.builder();
        builder.add(this.tarFile);
        this.chunkHelpers.values().forEach(chunkHelper -> {
          builder.add(chunkHelper.chunkFile);  
        });
        List<Path> files = builder.build().filter(Objects::nonNull).map(File::toPath).toList();
        // check that the non-null files do actually exist, are files and are readable
        return DepositUtils.filesExist(files);
    }
}
