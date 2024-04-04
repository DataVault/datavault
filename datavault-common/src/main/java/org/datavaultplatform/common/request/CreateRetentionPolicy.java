package org.datavaultplatform.common.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import org.jsondoc.core.annotation.ApiObject;
import org.jsondoc.core.annotation.ApiObjectField;

import java.util.Date;

@JsonIgnoreProperties(ignoreUnknown = true)
@ApiObject(name = "CreateRetentionPolicy")
@Data
public class CreateRetentionPolicy {

    @ApiObjectField(description = "Uh... the id")
    private int id;

    @ApiObjectField(description = "A name for the new Retention Policy")
    private String name;

    @ApiObjectField(description = "A description of Retention Policy")
    private String description;

    @ApiObjectField(description = "Engine for Retention Policy (deprecated")
    private String engine;

    @ApiObjectField(description = "Sorting order for Retention Policy (deprecated)")
    private String sort;

    @ApiObjectField(description = "URL for Retention Policy")
    private String url;

    @ApiObjectField(description = "Minimum retention period in years")
    private int minRetentionPeriod;

    @ApiObjectField(description = "Extend the expiry date of a vault if a deposit is retrieved")
    private boolean extendUponRetrieval;

    @ApiObjectField(description = "Minimum Date Retention Period (deprecated)")
    private String minDataRetentionPeriod;

    @ApiObjectField(description = "In Effect Date")
    private Date inEffectDate;

    @ApiObjectField(description = "End Date")
    private Date endDate;

    @ApiObjectField(description = "Date Guidance Reviewed")
    private Date dataGuidanceReviewed;



    public CreateRetentionPolicy() { }

    public int getId() {
        return id;
    }
    public int getID() {
        return getId();
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getEngine() {
        return engine;
    }

    public void setEngine(String engine) {
        this.engine = engine;
    }

    public String getSort() {
        return sort;
    }

    public void setSort(String sort) {
        this.sort = sort;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getMinRetentionPeriod() {
        return minRetentionPeriod;
    }

    public void setMinRetentionPeriod(int minRetentionPeriod) {
        this.minRetentionPeriod = minRetentionPeriod;
    }

    public boolean isExtendUponRetrieval() {
        return extendUponRetrieval;
    }

    public void setExtendUponRetrieval(boolean extendUponRetrieval) {
        this.extendUponRetrieval = extendUponRetrieval;
    }

    public String getMinDataRetentionPeriod() {
        return minDataRetentionPeriod;
    }

    public void setMinDataRetentionPeriod(String minDataRetentionPeriod) {
        this.minDataRetentionPeriod = minDataRetentionPeriod;
    }

    public Date getInEffectDate() {
        return inEffectDate;
    }

    public void setInEffectDate(Date inEffectDate) {
        this.inEffectDate = inEffectDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public Date getDataGuidanceReviewed() {
        return dataGuidanceReviewed;
    }

    public void setDataGuidanceReviewed(Date dataGuidanceReviewed) {
        this.dataGuidanceReviewed = dataGuidanceReviewed;
    }

    public String getPolicyInfo() {
        String retVal = this.id + "-" + this.minRetentionPeriod;
        return retVal;
    }

}
