package org.datavaultplatform.worker.tasks;

class TaskJsonAuditTest extends BaseTaskJsonTest<Audit> {

    @Override
    public Class<Audit> getTaskClass() {
        return Audit.class;
    }
}
