package org.datavaultplatform.worker.utils;

import static org.apache.commons.codec.digest.DigestUtils.sha1Hex;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Base64;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.datavaultplatform.common.crypto.Encryption;
import org.datavaultplatform.common.task.Context.AESMode;
import org.datavaultplatform.worker.utils.DecryptedTarBuilderParams.ChunkData;
import org.springframework.util.Assert;

@Slf4j
public class DecryptedTarBuilder {

  private final DecryptedTarBuilderParams params;

  public DecryptedTarBuilder(DecryptedTarBuilderParams params) {
    this.params = params;
  }

  public static List<Integer> getNumbersInRange(int start, int end) {
    return IntStream.rangeClosed(start, end)
        .boxed()
        .collect(Collectors.toList());
  }

  public static void main(String[] args) throws Exception {
    File file = new File(args[0]);
    log.info("Reading parameters from [{}]", file.getCanonicalPath());
    ObjectMapper mapper = new ObjectMapper();
    try (Reader fr = new BufferedReader(new FileReader(file))) {
      DecryptedTarBuilderParams params = mapper.readValue(fr, DecryptedTarBuilderParams.class);
      DecryptedTarBuilder builder = new DecryptedTarBuilder(params);
      File tarfile = builder.rebuildTarFile();
    }

  }

  @SneakyThrows
  public File rebuildTarFile() {

    File chunksDir = getDirectory(params.getChunksDir(), false);
    File tarBaseDir = getDirectory(params.getTarDir(), true);

    String timestamp = new SimpleDateFormat("ddMMMyyyy_HHmmss").format(new Date());
    File tarDir = new File(tarBaseDir, "tardir_" + timestamp);
    Files.createDirectory(tarDir.toPath());

    setupEncryption(params);

    TreeMap<Integer, ChunkFileInfo> chunks = getChunkFiles(chunksDir)
        .stream()
        .collect(Collectors.toMap(
            ChunkFileInfo::getChunkNumber,
            Function.identity(),
            (o1, o2) -> o1,
            TreeMap::new));

    Set<Integer> expectedChunkNumbers = new TreeSet<>(getNumbersInRange(1, chunks.size()));

    {
      Set<Integer> actualParamChunkNumbers = new TreeSet<>(params.getChunkData().keySet());
      Assert.isTrue(expectedChunkNumbers.equals(actualParamChunkNumbers), () ->
          String.format(
              "There is a problem with the parameter file chunkNumbers you have %s but should have %s",
              actualParamChunkNumbers, expectedChunkNumbers)
      );
    }

    {
      Set<Integer> actualTarFileChunkNumbers = chunks.keySet();
      Assert.isTrue(expectedChunkNumbers.equals(actualTarFileChunkNumbers), () ->
          String.format(
              "There is a problem with the tar file chunkNumbers you have %s but should have %s",
              actualTarFileChunkNumbers, expectedChunkNumbers)
      );
    }

    //add the iv data to the ChunkFileInfo
    chunks.forEach((chunkNum, chunkInfo) -> {
      String iv = params.getChunkData().get(chunkNum).getIv();
      if (StringUtils.isBlank(iv)) {
        throw new IllegalArgumentException(
            String.format("The chunk[%d] has a blank or missing iv", chunkNum));
      }
      chunkInfo.setIv(iv);
    });

    //copy the encrypted chunks in prep for decryption
    chunks.forEach((chunkNum, chunkInfo) -> {
      File decrypted = copyFileToDir(chunkInfo.getEncrypted(), tarDir);
      chunkInfo.setDecrypted(decrypted);
    });

    chunks.forEach((chunkNum, chunkInfo) -> {
      boolean isBase64 = org.apache.commons.codec.binary.Base64.isBase64(chunkInfo.getIv());
      Assert.isTrue( isBase64, () ->
          String.format("The iv for [%d] is NOT valid base64 string : [%s]", chunkNum, chunkInfo.getIv()));
    });

    params.getChunkData().forEach((chunkNum, chunkData) -> {

      validateExpectedChecksumFormat(chunkNum, "encrypted", chunkData.getEncryptedChecksum());
      validateExpectedChecksumFormat(chunkNum, "decrypted", chunkData.getDecryptedChecksum());

    });


    //decrypt the chunks
    chunks.forEach((chunkNum, chunkInfo) -> {
      File decrypted = chunkInfo.getDecrypted();

      byte[] iv = chunkInfo.getIVbytes();
      String encryptedChecksum = getSha1Checksum(decrypted);
      chunkInfo.setEncryptedChecksum(encryptedChecksum);

      decryptFile(decrypted, iv);
      String decryptedChecksum = getSha1Checksum(decrypted);
      chunkInfo.setDecryptedChecksum(decryptedChecksum);
    });

    String tarFileName = chunks.get(1).getPrefix() + ".tar";
    log.info("Tar File Name is [{}]", tarFileName);
    File finalTar = new File(tarDir, tarFileName);
    try (FileOutputStream fos = new FileOutputStream(finalTar)) {
      chunks.forEach((chunkNum, chunkInfo) -> copy(chunkInfo.getDecrypted(), fos));
    }
    outputChunkChecksums(chunks);
    outputTarChecksum(finalTar);
    listTarContents(finalTar);
    return finalTar;
  }

  void validateExpectedChecksumFormat(int chunk, String label, String value){
    if(StringUtils.isBlank(value)){
      return;
    }
    String sha1hexPattern = "[a-fA-F0-9]{40}";
    boolean isSha1Hex = value.matches(sha1hexPattern);
    Assert.isTrue(isSha1Hex, () -> String.format("Bad Format for expected [%s] checksum for chunk[%d] : not a sha1 hash in hex : [%s]",
        label, chunk, value));
  }


  private void outputTarChecksum(File finalTar) {
    log.info("Decrypted TAR checksum----------------");
    String checksum = getSha1Checksum(finalTar);
    String expectedTarChecksum = params.getTarChecksum();
    checkActualAgainstExpected("tar file", checksum, expectedTarChecksum);
    log.info("------------------------------------");
  }

  private void outputChunkChecksums(Map<Integer, ChunkFileInfo> chunks) {
    log.info("Encrypted checksums----------------");
    chunks.forEach((chunkNumber, info) -> {
      String actualEncryptedChecksum = info.getEncryptedChecksum();
      ChunkData cd = params.getChunkData().get(chunkNumber);
      Assert.isTrue(cd != null, () -> String.format("There is not chunkData for %d", chunkNumber));
      String expectedEncryptedChecksum = cd.getEncryptedChecksum();
      checkActualAgainstExpected(chunkNumber, actualEncryptedChecksum, expectedEncryptedChecksum);
    });

    log.info("Decrypted checksums----------------");
    chunks.forEach((chunkNumber, info) -> {
      String actualDecryptedChecksum = info.getDecryptedChecksum();
      ChunkData cd = params.getChunkData().get(chunkNumber);
      Assert.isTrue(cd != null);
      String expectedDecryptedChecksum = cd.getDecryptedChecksum();
      checkActualAgainstExpected(chunkNumber, actualDecryptedChecksum, expectedDecryptedChecksum);
    });
  }

  private void checkActualAgainstExpected(int chunkNumber, String actual, String expected) {
    checkActualAgainstExpected(String.format("[%3d]", chunkNumber), actual, expected);
  }

  private void checkActualAgainstExpected(String label, String actual, String expected) {
    log.info(String.format("%s [%s]", label, actual));
    if (actual.equalsIgnoreCase(expected)) {
      log.info(String.format("%s matches expected.", label));
    } else if (StringUtils.isNotBlank(expected)) {
      log.error(String.format("%s does NOT match expected. [%s]", label, expected));
    } else {
      log.info(String.format("%s none expected", label));
    }
  }

  @SneakyThrows
  private void listTarContents(File finalTar) {
    log.info("Listing contents of [{}]", finalTar);
    try (TarArchiveInputStream tis = new TarArchiveInputStream(
        new BufferedInputStream(Files.newInputStream(finalTar.toPath())))) {
      TarArchiveEntry entry;
      int count = 1;
      while ((entry = (TarArchiveEntry) tis.getNextEntry()) != null) {
        if (entry.isDirectory()) {
          continue;
        }
        log.info(String.format("[%3d][%s]", count, entry.getName()));
        count++;
      }
    }
  }

  @SneakyThrows
  private void copy(File file, FileOutputStream fos) {
    IOUtils.copy(file, fos);
  }

  @SneakyThrows
  private void decryptFile(File file, byte[] iv) {
    Encryption.decryptFile(AESMode.GCM, file, iv);
  }

  @SneakyThrows
  private File copyFileToDir(File file, File outputDir) {
    File copy = new File(outputDir, file.getName());
    try (OutputStream os = new BufferedOutputStream(Files.newOutputStream(copy.toPath()))) {
      IOUtils.copy(file, os);
    }
    return copy;
  }

  @SneakyThrows
  private void setupEncryption(DecryptedTarBuilderParams params) {
    Encryption.addBouncyCastleSecurityProvider();
    File keyStore = new File(params.getKeystorePath());
    Assert.isTrue(keyStore.exists(), String.format("The keystore [%s] does not exist.", keyStore));
    Assert.isTrue(keyStore.isFile(), String.format("The keystore [%s] is not a file.", keyStore));
    Assert.isTrue(keyStore.canRead(), String.format("The keystore [%s] cannot be read.", keyStore));
    Encryption.staticSetKeystorePath(keyStore.getCanonicalPath());
    Encryption.staticSetKeystorePassword(params.getKeystorePassword());
    @SuppressWarnings("WriteOnlyObject") Encryption enc = new Encryption();
    enc.setKeystoreEnable(true);
    enc.setVaultEnable(false);
    enc.setVaultDataEncryptionKeyName(params.getDataKeyName());
    enc.setVaultPrivateKeyEncryptionKeyName(null);
  }

  private List<ChunkFileInfo> getChunkFiles(File chunksDir) {
    File[] tarFiles = chunksDir.listFiles(pathname -> pathname.getName().contains(".tar."));

    assert tarFiles != null;

    Assert.isTrue(tarFiles.length > 0, () ->
        String.format("Could not find any encrypted, numbered chunks within [%s]", chunksDir));

    List<ChunkFileInfo> chunkFiles = Arrays.stream(tarFiles)
        .map(ChunkFileInfo::new)
        .sorted(Comparator.comparing(ChunkFileInfo::getChunkNumber))
        .collect(Collectors.toList());

    Set<String> tarNames = chunkFiles
        .stream().map(ChunkFileInfo::getPrefix)
        .collect(Collectors.toSet());

    if (tarNames.size() != 1) {
      throw new IllegalStateException(
          String.format("expected a single tarName but got %s", tarNames));
    }
    return chunkFiles;
  }

  private File getDirectory(String dir, boolean writable) {
    File file = new File(dir);
    Assert.isTrue(file.exists(), () -> String.format("The directory [%s] does not exist.", dir));
    Assert.isTrue(file.isDirectory(),
        () -> String.format("The file [%s] is NOT a directory.", dir));
    Assert.isTrue(file.canRead(), () -> String.format("The directory [%s] is NOT readable.", dir));
    if (writable) {
      Assert.isTrue(file.canWrite(),
          () -> String.format("The directory [%s] is NOT writable", dir));
    }
    return file;
  }

  @SneakyThrows
  private String getSha1Checksum(File file) {
    try (InputStream is = new BufferedInputStream(Files.newInputStream(file.toPath()))) {
      return sha1Hex(is).toUpperCase();
    }
  }

  @Data
  static
  class ChunkFileInfo {

    String prefix;
    int chunkNumber;
    File encrypted;
    String encryptedChecksum;

    File decrypted;
    String decryptedChecksum;
    String iv;

    public ChunkFileInfo(File encryptedChunkFileName) {
      String pattern = "(.*)(\\.tar\\.)(\\d+)";

      Pattern r = Pattern.compile(pattern);

      // Now create matcher object.
      Matcher m = r.matcher(encryptedChunkFileName.getName());

      if (m.find()) {
        this.prefix = m.group(1);
        this.chunkNumber = Integer.parseInt(m.group(3));
        this.encrypted = encryptedChunkFileName;
      } else {
        throw new IllegalArgumentException(
            String.format("The filename [%s] does not meet naming convention <prefix>.tar.<N>",
                encryptedChunkFileName.getName()));
      }
    }

    public byte[] getIVbytes() {
      byte[] result = Base64.getDecoder().decode(iv.getBytes(StandardCharsets.UTF_8));
      //for GCM - the iv byte should have length 96
      Assert.isTrue(result.length == 96,
          () -> String.format("The chunk[%d]iv byte array should be 96 length - it is [%d]",
              chunkNumber, result.length));
      return result;
    }
  }
}
