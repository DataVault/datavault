package org.datavaultplatform.common.response;

import java.util.List;

import org.jsondoc.core.annotation.ApiObject;
import org.jsondoc.core.annotation.ApiObjectField;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
@ApiObject(name = "VaultsData")
public class VaultsData {
	
	@ApiObjectField(description = "Total records, before filtering")
    private int recordsTotal;

    /**
     * Total records, after filtering (i.e. the total number of records after
     * filtering has been applied - not just the number of records being returned
     * for this page of data).
     */
	@ApiObjectField(description = "Total records, after filtering")
    private int recordsFiltered;

    /**
     * The data to be displayed in the table. 
     */
	@ApiObjectField(description = "The Vaults information to be displayed in the table")
    private List<VaultInfo> data;
    
	public VaultsData() {
		
	}
	
    public VaultsData(int recordsTotal, int recordsFiltered, List<VaultInfo> data) {
		super();
		this.recordsTotal = recordsTotal;
		this.recordsFiltered = recordsFiltered;
		this.data = data;
	}

	public List<VaultInfo> getData() {
		return data;
	}

	public void setData(List<VaultInfo> data) {
		this.data = data;
	}

	public int getRecordsTotal() {
		return recordsTotal;
	}
	
	public void setRecordsTotal(int recordsTotal) {
		this.recordsTotal = recordsTotal;
	}
	
	public int getRecordsFiltered() {
		return recordsFiltered;
	}
	
	public void setRecordsFiltered(int recordsFiltered) {
		this.recordsFiltered = recordsFiltered;
	}

}
