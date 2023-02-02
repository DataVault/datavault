package org.datavaultplatform.worker.utils;


import java.io.File;
import org.datavaultplatform.worker.operations.Tar;
import org.springframework.util.Assert;

public class UnTarBuilder {


  public static void main(String[] args) throws Exception {

    Assert.isTrue(args.length==2, () -> "2 arguments are required");
    File inputTarFile = new File(args[0]);

    Assert.isTrue(inputTarFile.exists(), () -> String.format("The intputTarFile [%s] does not exist", inputTarFile));
    Assert.isTrue(inputTarFile.canRead(), () -> String.format("The intputTarFile [%s] is not readable", inputTarFile));
    Assert.isTrue(inputTarFile.isFile(), () -> String.format("The intputTarFile [%s] is not a file", inputTarFile));
    Assert.isTrue(inputTarFile.getAbsolutePath().endsWith(".tar"), () -> String.format("The intputTarFile [%s] does not end in .tar", inputTarFile));

    File outputDirectory = new File(args[1]);
    Assert.isTrue(outputDirectory.exists(), () -> String.format("The outputDirectory [%s] does not exist.", outputDirectory));
    Assert.isTrue(outputDirectory.canRead(), () -> String.format("The outputDirectory [%s] is not readable.", outputDirectory));
    Assert.isTrue(outputDirectory.canWrite(), () -> String.format("The outputDirectory [%s] is not writable.", outputDirectory));
    Assert.isTrue(outputDirectory.isDirectory(), () -> String.format("The outputDirectory [%s] is not a directory.", outputDirectory));

    Assert.isTrue(outputDirectory.listFiles().length == 0, () -> String.format("The outputDirectory [%s] is not empty.", outputDirectory));

    Tar.unTar(inputTarFile, outputDirectory.toPath());
  }

}
