package org.datavaultplatform.common.storage;

import static org.junit.Assert.assertEquals;

import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.jupiter.api.Test;

public class PathTest {

  @Test
  void test() {
    assertEquals(".", removeLeadingSlash("/"));
    assertEquals("a/b/c", removeLeadingSlash("/a/b/c"));
    assertEquals("a/b/c", removeLeadingSlash("a/b/c"));
  }

  Path removeLeadingSlash(Path path) {
    if(path.getNameCount() == 0) {
      return Paths.get(".");
    } else if(path.getRoot() == null) {
      return path;
    } else {
      return path.subpath(0, path.getNameCount());
    }
  }

  String removeLeadingSlash(String path){
    return removeLeadingSlash(Paths.get(path)).toString();
  }
}
