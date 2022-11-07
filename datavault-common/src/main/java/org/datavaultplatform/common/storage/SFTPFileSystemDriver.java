package org.datavaultplatform.common.storage;

import java.io.File;
import java.util.List;
import org.datavaultplatform.common.io.Progress;
import org.datavaultplatform.common.model.FileInfo;

public interface SFTPFileSystemDriver extends UserStore {

  @Override
  List<FileInfo> list(String path);

  @Override
  boolean valid(String path);

  @Override
  boolean exists(String path);

  @Override
  long getSize(String path) throws Exception;

  @Override
  boolean isDirectory(String path) throws Exception;

  @Override
  String getName(String path);

  long getUsableSpace() throws Exception;

  void retrieve(String path, File working, Progress progress) throws Exception;

  String store(String path, File working, Progress progress) throws Exception;

  boolean isMonitoring();

}
