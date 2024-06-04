package org.datavaultplatform.worker.tasks;

import lombok.SneakyThrows;
import org.datavaultplatform.worker.tasks.deposit.DepositUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public record DepositTransferHelper(Path bagPath, Path bagDataPath) {

    public DepositTransferHelper(Path bagPath) {
        this(bagPath, bagPath.resolve("data"));
    }
    
    public boolean directoriesExist(){ 
        return DepositUtils.directoriesExist(List.of(bagPath, bagDataPath));
    }
    
    @SneakyThrows
    public void createDirs() {
        Files.createDirectories(this.bagPath);
        Files.createDirectories(this.bagDataPath);
    }
}
