package org.datavaultplatform.common.model;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Version;

import org.datavaultplatform.common.response.BillingInformation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


@JsonIgnoreProperties(ignoreUnknown = true)
@Entity
@Table(name="BillingInfo")
public class BillingInfo {

	private static final long ZERO = 0l;
    // Vault Identifier
    @Id
    @Column(name = "id", unique = true)
    private String id;
    
    // Hibernate version
    @Version
    private long version;
     
    @Column(name = "contactName", nullable = true, columnDefinition = "TEXT", length=400)
    private String contactName;
   
	@Column(name = "subUnit", nullable = true, columnDefinition = "TEXT", length=400)
    private String subUnit;
    
    @Column(name = "budgetCode", nullable = true, columnDefinition = "BIT", length = 1)
    private Boolean budgetCode;
    
    @Column(name = "specialComments", nullable = true, columnDefinition = "TEXT", length=400)
    private String specialComments;
    
    @Column(name = "amountToBeBilled", nullable = true, columnDefinition="Decimal(15,2) default '000.00'")
    private BigDecimal amountToBeBilled;
    
    @Column(name = "amountBilled", nullable = true, columnDefinition="Decimal(15,2) default '000.00'")
    private BigDecimal amountBilled;
    
	@Column(name = "school", nullable = true, columnDefinition = "TEXT", length=400)
    private String school;
	
	@OneToOne
	@JoinColumn(name="vaultID")
    private Vault vault;
    	 
   
	public String getContactname() {
		return contactName;
	}
	public void setContactname(String contactname) {
		this.contactName = contactname;
	}
	
    public String getSchool() {
		return school;
	}
	public void setSchool(String school) {
		this.school = school;
	}
	public String getSubunit() {
		return subUnit;
	}
	public void setSubunit(String subunit) {
		this.subUnit = subunit;
	}
	public Boolean getBudgetcode() {
		return budgetCode;
	}
	public void setBudgetcode(Boolean budgetcode) {
		this.budgetCode = budgetcode;
	}
	public String getSpecialcomments() {
		return specialComments;
	}
	public void setSpecialcomments(String specialcomments) {
		this.specialComments = specialcomments;
	}
	   
    public BigDecimal getAmountToBeBilled() {
		return amountToBeBilled;
	}
	public void setAmountToBeBilled(BigDecimal amountToBeBilled) {
		this.amountToBeBilled = amountToBeBilled;
	}
	public BigDecimal getAmountBilled() {
		return amountBilled;
	}
	public void setAmountBilled(BigDecimal amountBilled) {
		this.amountBilled = amountBilled;
	}

    public Vault getVault() {
		return vault;
	}
	public void setVault(Vault vault) {
		this.vault = vault;
	}
	public BillingInfo() {}
 
    public String getID() { 
    	return id; 
    }

    public long getVersion() {
    	return version; 
    };
     
    
    @Override
    public boolean equals(Object other) {
        
        if (this == other) return true;
        
        if (!(other instanceof BillingInfo)) {
            return false;
        }
        
        final BillingInfo vault = (BillingInfo)other;
        
        if (!vault.getID().equals(getID())) {
            return false;
        }
        
        return true;
    }
    
    @Override
    public int hashCode() {
        return getID().hashCode();
    }
}
