package org.datavaultplatform.broker.services;

import jakarta.transaction.Transactional;
import org.datavaultplatform.common.model.PausedDepositState;
import org.datavaultplatform.common.model.dao.PausedDepositStateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PausedDepositStateService {

    private static final PausedDepositState NOT_PAUSED;
    
    static {
        NOT_PAUSED = new PausedDepositState();
        NOT_PAUSED.setPaused(false);
        NOT_PAUSED.setCreated(LocalDateTime.of(1970,1,1,12,0,0));
    }
    private final PausedDepositStateRepository repo;

    @Autowired
    public PausedDepositStateService(PausedDepositStateRepository repo) {
        this.repo = repo;
    }

    public PausedDepositState getCurrentState() {
        return repo.getCurrentState().orElse(NOT_PAUSED);
    }

    @Transactional
    public PausedDepositState toggleState() {
        PausedDepositState current = getCurrentState();
        boolean newPaused = !current.isPaused();
        PausedDepositState newState = new PausedDepositState();
        newState.setPaused(newPaused);
        return repo.save(newState);
    }

    public List<PausedDepositState> getRecentEntries(int limit){
        return repo.getRecentEntries(limit);
    }
}
