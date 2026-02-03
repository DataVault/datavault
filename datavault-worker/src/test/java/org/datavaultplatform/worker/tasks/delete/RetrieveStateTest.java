package org.datavaultplatform.worker.tasks.delete;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

class DeleteStateTest {

    @Test
    void testGetDeleteStates(){
        ArrayList<String> states = new ArrayList<>();
        states.add("Deleting from archive"); // 0
        states.add("Delete complete");  // 1
        assertThat(DeleteState.getDeleteStates()).isEqualTo(states);
    }

}