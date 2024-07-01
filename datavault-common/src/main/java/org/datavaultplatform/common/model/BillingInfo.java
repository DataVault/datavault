package org.datavaultplatform.common.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;
import java.util.Objects;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.NamedAttributeNode;
import jakarta.persistence.NamedEntityGraph;
import jakarta.persistence.NamedSubgraph;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import org.datavaultplatform.common.response.BillingInformation;
import org.hibernate.Hibernate;
import org.hibernate.annotations.UuidGenerator;

@JsonIgnoreProperties(ignoreUnknown = true)
@Entity
@Table(name = "BillingInfo")
@NamedEntityGraph(name = BillingInfo.EG_BILLING_INFO, attributeNodes = @NamedAttributeNode(value = BillingInfo_.VAULT, subgraph = "subVault"), subgraphs = @NamedSubgraph(name = "subVault", attributeNodes = {
		@NamedAttributeNode(Vault_.DATASET),
		@NamedAttributeNode(Vault_.GROUP),
		@NamedAttributeNode(Vault_.RETENTION_POLICY),
		@NamedAttributeNode(Vault_.USER)
}))
public class BillingInfo {

	public static final String EG_BILLING_INFO = "eg.BillingInfo.1";
	@Id
	@UuidGenerator
	@Column(name = "id", unique = true, length = 36)
	private String id;

	// Hibernate version
	@Version
	private long version;

	@Column(name = "contactName", nullable = true, columnDefinition = "TEXT", length = 400)
	private String contactName;

	@Column(name = "subUnit", nullable = true, columnDefinition = "TEXT", length = 400)
	private String subUnit;

	@Column(name = "budgetCode", nullable = true, columnDefinition = "BIT", length = 1)
	private Boolean budgetCode;

	@Column(name = "specialComments", nullable = true, columnDefinition = "TEXT", length = 400)
	private String specialComments;

	@Column(name = "amountToBeBilled", nullable = true, columnDefinition = "Decimal(15,2) default '000.00'")
	private BigDecimal amountToBeBilled;

	@Column(name = "amountBilled", nullable = true, columnDefinition = "Decimal(15,2) default '000.00'")
	private BigDecimal amountBilled;

	@Column(name = "school", nullable = true, columnDefinition = "TEXT", length = 400)
	private String school;

	@Column(name = "billingType", nullable = false, columnDefinition = "TEXT", length = 10)
	private PendingVault.Billing_Type billingType;

	@Column(name = "sliceID", nullable = true, columnDefinition = "TEXT", length = 40)
	private String sliceID;

	@Column(name = "projectTitle", nullable = true, columnDefinition = "TEXT", length = 400)
	private String projectTitle;

	@Column(name = "paymentDetails", nullable = true, columnDefinition = "TEXT")
	private String paymentDetails;

	@OneToOne
	@JoinColumn(name = "vaultID", nullable = false)
	private Vault vault;

	public String getContactName() {
		return contactName;
	}

	public void setContactName(String contactName) {
		this.contactName = contactName;
	}

	public String getSchool() {
		return school;
	}

	public void setSchool(String school) {
		this.school = school;
	}

	public String getSubUnit() {
		return subUnit;
	}

	public void setSubUnit(String subUnit) {
		this.subUnit = subUnit;
	}

	public Boolean getBudgetCode() {
		return budgetCode;
	}

	public void setBudgetCode(Boolean budgetCode) {
		this.budgetCode = budgetCode;
	}

	public String getSpecialComments() {
		return specialComments;
	}

	public void setSpecialComments(String specialComments) {
		this.specialComments = specialComments;
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

	public PendingVault.Billing_Type getBillingType() {
		return this.billingType;
	}

	public void setBillingType(PendingVault.Billing_Type billingType) {
		this.billingType = billingType;
	}

	public String getSliceID() {
		return this.sliceID;
	}

	public void setSliceID(String sliceID) {
		this.sliceID = sliceID;
	}

	public String getProjectTitle() {
		return this.projectTitle;
	}

	public void setProjectTitle(String projectTitle) {
		this.projectTitle = projectTitle;
	}

	public BillingInfo() {
	}

	public String getID() {
		return id;
	}

	public long getVersion() {
		return version;
	}

	public String getPaymentDetails() {
		return this.paymentDetails;
	}

	public void setPaymentDetails(String paymentDetails) {
		this.paymentDetails = paymentDetails;
	}

	public BillingInformation convertToResponse() {
		return new BillingInformation(
				id,
				vault.getID(),
				contactName,
				school,
				subUnit,
				budgetCode,
				specialComments,
				amountToBeBilled,
				amountBilled,
				vault.getProjectId(),
				vault.getName(),
				sliceID,
				projectTitle,
				billingType,
				paymentDetails);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) {
			return false;
		}
		BillingInfo that = (BillingInfo) o;
		return id != null && Objects.equals(id, that.id);
	}

	@Override
	public int hashCode() {
		return getClass().hashCode();
	}
}
