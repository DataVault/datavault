package org.datavaultplatform.broker.services;

import jakarta.transaction.Transactional;
import org.datavaultplatform.common.model.PausedState;
import org.datavaultplatform.common.model.dao.PausedStateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PausedStateService {

    private static final PausedState NOT_PAUSED;
    
    static {
        NOT_PAUSED = new PausedState();
        NOT_PAUSED.setPaused(false);
        NOT_PAUSED.setCreated(LocalDateTime.of(1970,1,1,12,0,0));
    }
    private final PausedStateRepository repo;

    @Autowired
    public PausedStateService(PausedStateRepository repo) {
        this.repo = repo;
    }

    public PausedState getCurrentState() {
        return repo.getCurrentState().orElse(NOT_PAUSED);
    }

    @Transactional
    public PausedState toggleState() {
        PausedState current = getCurrentState();
        boolean newPaused = !current.isPaused();
        PausedState newState = new PausedState();
        newState.setPaused(newPaused);
        return repo.save(newState);
    }

    public List<PausedState> getRecentEntries(int limit){
        return repo.getRecentEntries(limit);
    }
}
