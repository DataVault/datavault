package org.datavaultplatform.worker.utils;


import java.io.File;
import org.datavaultplatform.worker.operations.Tar;
import org.springframework.util.Assert;

public class TarBuilder {


  public static void main(String[] args) throws Exception {

    Assert.isTrue(args.length==2, () -> "2 arguments are required");
    File inputDir = new File(args[0]);

    Assert.isTrue(inputDir.exists(), () -> String.format("The intputDir [%s] does not exist", inputDir));
    Assert.isTrue(inputDir.canRead(), () -> String.format("The intputDir [%s] is not readable", inputDir));
    Assert.isTrue(inputDir.isDirectory(), () -> String.format("The intputDir [%s] is not a directory", inputDir));

    File outputFile = new File(args[1]);
    Assert.isTrue(!outputFile.exists(), () -> String.format("The outfile [%s] already exists", outputFile));

    Tar.createTar(inputDir, outputFile);
  }

}
