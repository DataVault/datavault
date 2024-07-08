package org.datavaultplatform.common.task;

import lombok.Getter;

@Getter
public sealed class TaskStage permits TaskStage.DepositTaskStage, TaskStage.RetrieveTaskStage {
    
    private final TaskType type;
    private final int order;
    private final String label;
    
    private TaskStage(TaskType type, int order) {
            this.type = type;
            this.order = order;
            this.label = this.getClass().getSimpleName();
    }
    
    public static sealed class DepositTaskStage extends TaskStage permits 
            Deposit1ComputeSize,
            Deposit2Transfer,
            Deposit3PackageEncrypt,
            Deposit4Archive,
            Deposit5Verify {
        public DepositTaskStage(int order) {
            super(TaskType.Deposit, order);
        }
    }
    public static final class Deposit1ComputeSize extends DepositTaskStage {
        private Deposit1ComputeSize() {
            super(1);
        }
        public static final Deposit1ComputeSize INSTANCE = new Deposit1ComputeSize();
    }
    public static final class Deposit2Transfer extends DepositTaskStage {
        private Deposit2Transfer() {
            super(2);
        }
        public static final Deposit2Transfer INSTANCE = new Deposit2Transfer();
    }
    public static final class Deposit3PackageEncrypt extends DepositTaskStage {
        private Deposit3PackageEncrypt() {
            super(3);
        }
        public static final Deposit3PackageEncrypt INSTANCE = new Deposit3PackageEncrypt();
    }
    public static final class Deposit4Archive extends DepositTaskStage {
        private Deposit4Archive() {
            super(4);
        }
        public static final Deposit4Archive INSTANCE = new Deposit4Archive();
    }
    public static final class Deposit5Verify extends DepositTaskStage {
        private Deposit5Verify() {
            super(5);
        }
        public static final Deposit5Verify INSTANCE = new Deposit5Verify();
    }
    public static non-sealed class RetrieveTaskStage extends TaskStage {
        public RetrieveTaskStage(int order) {
            super(TaskType.Retrieve, order);
        }
    }
}
