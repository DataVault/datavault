package org.datavaultplatform.broker.services;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.common.model.FileFixity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@Slf4j
public class MetadataServiceTest {

  MetadataService service;

  @BeforeEach
  void setup() {
    File metaDataDir = new File("src/test/resources/metadata");
    service = new MetadataService(metaDataDir.getAbsolutePath());
  }

  @Test
  void testGetManifestForBag01() {
    List<FileFixity> result = service.getManifest("bag01");
    result.forEach(ff -> log.info("ff [{}]", ff));
    assertEquals(6, result.size());

    Map<String,FileFixity> ffByName = result.stream()
        .collect(Collectors.toMap(ff -> ff.getFile(), ff -> ff));

    FileFixity ff1 = ffByName.get("item1.pdf");
    assertEquals("item1.pdf", ff1.getFile());
    assertEquals("md5", ff1.getAlgorithm());
    assertEquals("681eb85da8d6709b03e1016a5913a13c", ff1.getFixity());
    assertEquals("application/pdf", ff1.getFileType());

    FileFixity ff2 = ffByName.get("item2.pdf");
    assertEquals("item2.pdf", ff2.getFile());
    assertEquals("md5", ff2.getAlgorithm());
    assertEquals("681eb85da8d6709b03e1016a5913a13d", ff2.getFixity());
    assertEquals("", ff2.getFileType());

    FileFixity ff3 = ffByName.get("item3.pdf");
    assertEquals("item3.pdf", ff3.getFile());
    assertEquals("md5", ff3.getAlgorithm());
    assertEquals("681eb85da8d6709b03e1016a5913a13e", ff3.getFixity());
    assertEquals("application/x-pdf", ff3.getFileType());

    FileFixity ff4 = ffByName.get("item4.pdf");
    assertEquals("item4.pdf", ff4.getFile());
    assertEquals("sha1", ff4.getAlgorithm());
    assertEquals("3b108e011931fff26cbb908a32ebfebadf2d922b", ff4.getFixity());
    assertEquals("application/pdf", ff4.getFileType());

    FileFixity ff5 = ffByName.get("item5.pdf");
    assertEquals("item5.pdf", ff5.getFile());
    assertEquals("sha1", ff5.getAlgorithm());
    assertEquals("3b108e011931fff26cbb908a32ebfebadf2d922c", ff5.getFixity());
    assertEquals("", ff5.getFileType());

    FileFixity ff6 = ffByName.get("item6.pdf");
    assertEquals("item6.pdf", ff6.getFile());
    assertEquals("sha1", ff6.getAlgorithm());
    assertEquals("3b108e011931fff26cbb908a32ebfebadf2d922d", ff6.getFixity());
    assertEquals("application/x-pdf", ff6.getFileType());
  }
}
