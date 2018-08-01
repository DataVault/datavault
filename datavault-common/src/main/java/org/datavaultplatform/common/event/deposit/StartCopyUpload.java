package org.datavaultplatform.common.event.deposit;

import javax.persistence.Entity;
import org.datavaultplatform.common.event.Event;

@Entity
public class StartCopyUpload extends Event {

	//public String type;

	StartCopyUpload() {
		
	};

	public StartCopyUpload(String jobId, String depositId, String type) {
		super("Copy Upload started - (" + type + ")");
		this.eventClass = StartCopyUpload.class.getCanonicalName();
		this.depositId = depositId;
		this.jobId = jobId;
		//this.type = type;
	}
	
//	public String getType() {
//		return this.type;
//	}
//
//	public void setType(String type) {
//		this.type = type;
//	}
}
