package org.datavaultplatform.worker.tasks;

class TaskJsonDeleteTest extends BaseTaskJsonTest<Delete> {

    @Override
    public Class<Delete> getTaskClass() {
        return Delete.class;
    }
}
