package org.datavaultplatform.broker.services;

import jakarta.transaction.Transactional;
import org.datavaultplatform.common.model.PausedRetrieveState;
import org.datavaultplatform.common.model.dao.PausedRetrieveStateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PausedRetrieveStateService {

    private static final PausedRetrieveState NOT_PAUSED;
    
    static {
        NOT_PAUSED = new PausedRetrieveState();
        NOT_PAUSED.setPaused(false);
        NOT_PAUSED.setCreated(LocalDateTime.of(1970,1,1,12,0,0));
    }
    private final PausedRetrieveStateRepository repo;

    @Autowired
    public PausedRetrieveStateService(PausedRetrieveStateRepository repo) {
        this.repo = repo;
    }

    public PausedRetrieveState getCurrentState() {
        return repo.getCurrentState().orElse(NOT_PAUSED);
    }

    @Transactional
    public PausedRetrieveState toggleState() {
        PausedRetrieveState current = getCurrentState();
        boolean newPaused = !current.isPaused();
        PausedRetrieveState newState = new PausedRetrieveState();
        newState.setPaused(newPaused);
        return repo.save(newState);
    }

    public List<PausedRetrieveState> getRecentEntries(int limit){
        return repo.getRecentEntries(limit);
    }
}
