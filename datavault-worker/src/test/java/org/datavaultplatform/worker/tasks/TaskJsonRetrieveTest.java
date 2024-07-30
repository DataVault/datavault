package org.datavaultplatform.worker.tasks;

class TaskJsonRetrieveTest extends BaseTaskJsonTest<Retrieve> {

    @Override
    public Class<Retrieve> getTaskClass() {
        return Retrieve.class;
    }
}
