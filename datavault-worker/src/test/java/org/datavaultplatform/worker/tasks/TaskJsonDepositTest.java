package org.datavaultplatform.worker.tasks;

class TaskJsonDepositTest extends BaseTaskJsonTest<Deposit> {

    @Override
    public Class<Deposit> getTaskClass() {
        return Deposit.class;
    }
}
