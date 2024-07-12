package org.datavaultplatform.common.task;

import lombok.Getter;

import java.util.Comparator;

@Getter
public sealed class TaskStage implements Comparable<TaskStage>
        permits TaskStage.DepositTaskStage, TaskStage.RetrieveTaskStage {

    public static final Comparator<TaskStage> COMPARATOR = Comparator
            .comparing(TaskStage::getType)
            .thenComparing(TaskStage::getOrder);
    
    private final TaskType type;
    private final int order;
    private final String label;
    
    private TaskStage(TaskType type, int order) {
            this.type = type;
            this.order = order;
            this.label = this.getClass().getSimpleName();
    }

    @Override
    public int compareTo(TaskStage other) {
        return COMPARATOR.compare(this, other);
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
    
    public static sealed class RetrieveTaskStage extends TaskStage permits
            Retrieve1checkUserStoreFreeSpace,
            Retrieve2retrieveFromArchiveAndRecompose,
            Retrieve3uploadedToUserStore
    {
        public RetrieveTaskStage(int order) {
            super(TaskType.Retrieve, order);
        }
    }
    public static final class Retrieve1checkUserStoreFreeSpace extends RetrieveTaskStage {
        private Retrieve1checkUserStoreFreeSpace() { super(1); }
        public static final Retrieve1checkUserStoreFreeSpace INSTANCE = new Retrieve1checkUserStoreFreeSpace();
    }
    public static final class Retrieve2retrieveFromArchiveAndRecompose extends RetrieveTaskStage {
        private Retrieve2retrieveFromArchiveAndRecompose() { super(2); }
        public static final Retrieve2retrieveFromArchiveAndRecompose INSTANCE = new Retrieve2retrieveFromArchiveAndRecompose();
    }
    public static final class Retrieve3uploadedToUserStore extends RetrieveTaskStage {
        private Retrieve3uploadedToUserStore() { super(3); }
        public static final Retrieve3uploadedToUserStore INSTANCE = new Retrieve3uploadedToUserStore();
    }
}
