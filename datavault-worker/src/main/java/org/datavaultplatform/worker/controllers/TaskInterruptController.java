package org.datavaultplatform.worker.controllers;

import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.common.event.Event;
import org.datavaultplatform.common.event.deposit.*;
import org.datavaultplatform.common.event.retrieve.*;
import org.datavaultplatform.common.task.TaskInterrupter;
import org.datavaultplatform.common.task.TaskStage;
import org.datavaultplatform.common.task.TaskStageSupport;
import org.datavaultplatform.worker.rabbit.RabbitMessageSelectorScheduler;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/task/interrupt")
public class TaskInterruptController {

    private final RabbitMessageSelectorScheduler scheduler;

    protected String interruptStageName;

    public TaskInterruptController(RabbitMessageSelectorScheduler scheduler) {
        this.scheduler = scheduler;
    }

    public static Class<? extends Event> getLastEventForTaskStage(TaskStage taskStage) {
        // RETRIEVE STAGES
        if (taskStage == TaskStage.Retrieve1CheckUserStoreFreeSpace.INSTANCE) {
            return UserStoreSpaceAvailableChecked.class;
        } else if (taskStage == TaskStage.Retrieve2RetrieveFromArchiveAndRecompose.INSTANCE) {
            return ArchiveStoreRetrievedAll.class;
        } else if (taskStage == TaskStage.Retrieve3UploadedToUserStore.INSTANCE) {
            return UploadedToUserStore.class;
        } else if (taskStage == TaskStage.Retrieve4Final.INSTANCE) {
            return RetrieveComplete.class;
            // DEPOSIT STAGES
        } else if (taskStage == TaskStage.Deposit1ComputeSize.INSTANCE) {
            return ComputedSize.class;
        } else if (taskStage == TaskStage.Deposit2Transfer.INSTANCE) {
            return TransferComplete.class;
        } else if (taskStage == TaskStage.Deposit3PackageEncrypt.INSTANCE) {
            return ComputedEncryption.class; //assume encrypt with multi-chunk
        } else if (taskStage == TaskStage.Deposit4Archive.INSTANCE) {
            return UploadComplete.class;
        } else if (taskStage == TaskStage.Deposit5Verify.INSTANCE) {
            return ValidationComplete.class;
        } else if (taskStage == TaskStage.Deposit6Final.INSTANCE) {
            return ValidationComplete.class;
        } else {
            return null;
        }
    }

    private void setChecker(String stageName, TaskInterrupter.Checker checker){
        interruptStageName = stageName;
        scheduler.setChecker(checker);
        log.info("interrupt:stageName[{}]", interruptStageName);
    }
    
    @DeleteMapping
    public synchronized void deleteInterrupt() {
        setChecker(null, null);
    }

    @GetMapping
    public synchronized TaskInterrupt getInterrupt() {
        return new TaskInterrupt(interruptStageName);
    }

    @PostMapping
    public ResponseEntity<?> setInterrupt(@RequestBody TaskInterrupt taskInterrupt) {
        Assert.isTrue(taskInterrupt != null, "TaskInterrupt must not be null");
        try {
            Optional<Class<? extends Event>> optLastEventOfStage = TaskStageSupport.getTaskStage(taskInterrupt.stageName())
                    .map(TaskInterruptController::getLastEventForTaskStage);
            if (optLastEventOfStage.isEmpty()) {
                throw new RuntimeException("Could not resolve taskInterrupt stage [%s]".formatted(taskInterrupt.stageName()));
            }
            Class<? extends Event> lastEventOfStage = optLastEventOfStage.get();
            
            var checker = new TaskInterrupter.Checker(event -> lastEventOfStage.getName().equals(event.getClass().getName()), lastEventOfStage.getSimpleName());
            setChecker(taskInterrupt.stageName(), checker);
            
            return ResponseEntity.ok(taskInterrupt);
        } catch (Exception ex) {
            String msg = "ERROR : [%s]".formatted(ex.getMessage());
            log.error(msg, ex);
            return ResponseEntity.badRequest().body(msg);
        }
    }

    @GetMapping("/all")
    public List<TaskInterrupt> getAllTaskInterrupts() {
        return TaskStageSupport.TASK_STAGE_MAP.keySet().stream().map(TaskInterrupt::new).toList();
    }
    
    

    
}
