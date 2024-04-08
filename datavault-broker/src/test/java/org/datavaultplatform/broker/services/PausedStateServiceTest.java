package org.datavaultplatform.broker.services;


import org.datavaultplatform.common.model.PausedState;
import org.datavaultplatform.common.model.dao.PausedStateRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PausedStateServiceTest {

    @Captor
    ArgumentCaptor<Integer> argLimit;

    @Captor
    ArgumentCaptor<PausedState> argState;

    @Mock
    PausedStateRepository mRepo;

    @InjectMocks
    PausedStateService service;
    
    @Test
    void testCurrentStateExists() {

        PausedState state = new PausedState();
        when(mRepo.getCurrentState()).thenReturn(Optional.of(state));

        PausedState initial = service.getCurrentState();

        assertThat(initial).isSameAs(state);

        verify(mRepo).getCurrentState();
        verifyNoMoreInteractions(mRepo);
    }

    @Test
    void testCurrentStateNotExists() {

        when(mRepo.getCurrentState()).thenReturn(Optional.empty());

        PausedState initial = service.getCurrentState();
        assertThat(initial).isNotNull();
        assertThat(initial.isPaused()).isFalse();
        assertThat(initial.getCreated().toLocalDate()).isEqualTo(LocalDate.of(1970, 1, 1));

        verify(mRepo).getCurrentState();
        verifyNoMoreInteractions(mRepo);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void toggleState(boolean currentlyIsPaused) {

        PausedState state = new PausedState();
        state.setPaused(currentlyIsPaused);
        when(mRepo.getCurrentState()).thenReturn(Optional.of(state));

        when(mRepo.save(argState.capture())).thenReturn(null);

        service.toggleState();

        verify(mRepo).getCurrentState();

        PausedState actualPausedState = argState.getValue();
        assertThat(actualPausedState.isPaused()).isNotEqualTo(currentlyIsPaused);

        verify(mRepo).save(actualPausedState);

    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2, 3, 4})
    void testGetRecentEntries(int limit) {
        List<PausedState> entries = new ArrayList<>();
        when(mRepo.getRecentEntries(argLimit.capture())).thenReturn(entries);

        service.getRecentEntries(limit);

        int actualLimit = argLimit.getValue();

        assertThat(actualLimit).isEqualTo(limit);
        
        verify(mRepo).getRecentEntries(actualLimit);

    }
}