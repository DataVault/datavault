package org.datavaultplatform.common.model.dao;

import org.datavaultplatform.common.model.DepositChunk;

import java.util.List;

public interface DepositChunkDAO {

    public void save(DepositChunk deposit);
    
    public void update(DepositChunk deposit);
    
    public List<DepositChunk> list(String sort);

    public DepositChunk findById(String Id);
}
