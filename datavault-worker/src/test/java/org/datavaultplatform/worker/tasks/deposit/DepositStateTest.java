package org.datavaultplatform.worker.tasks.deposit;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

class DepositStateTest {

    @Test
     void testGetDepositStates() {
             ArrayList<String> expected = new ArrayList<>();
             expected.add("Calculating size");     // 0
             expected.add("Transferring");         // 1
             expected.add("Packaging");            // 2
             expected.add("Storing in archive");   // 3
             expected.add("Verifying");            // 4
             expected.add("Complete");             // 5
        
        assertThat(DepositState.getDepositStates()).isEqualTo(expected);
     }
}