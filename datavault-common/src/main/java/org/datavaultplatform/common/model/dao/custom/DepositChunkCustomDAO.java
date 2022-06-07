package org.datavaultplatform.common.model.dao.custom;

import java.util.List;
import org.datavaultplatform.common.model.DepositChunk;

public interface DepositChunkCustomDAO {

    public void save(DepositChunk deposit);
    
    public void update(DepositChunk deposit);
    
    public List<DepositChunk> list(String sort);

    public DepositChunk findById(String Id);
}
