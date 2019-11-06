package org.datavaultplatform.common.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.jsondoc.core.annotation.ApiObject;
import org.jsondoc.core.annotation.ApiObjectField;

@JsonIgnoreProperties(ignoreUnknown = true)
@ApiObject(name = "CreateRetentionPolicy")
public class CreateRetentionPolicy {
    @ApiObjectField(description = "A name for the new Retention Policy")
    private String name;

    @ApiObjectField(description = "A description of Retention Policy")
    private String description;

    @ApiObjectField(description = "Engine for Retention Policy")
    private String engine;

    @ApiObjectField(description = "Sorting order for Retention Policy (deprecated)")
    private String sort;

    @ApiObjectField(description = "URL for Retention Policy")
    private String url;

    @ApiObjectField(description = "Minimum Date Retention Period")
    private String minDataRetentionPeriod;

    @ApiObjectField(description = "In Effect Date")
    private String inEffectDate;

    @ApiObjectField(description = "Date Guidance Reviewed")
    private String dateGuidanceReviewed;

    @ApiObjectField(description = "End Date")
    private String endDate;

    public CreateRetentionPolicy() { }
    public CreateRetentionPolicy(String name, String description, String sort, String url, String engine,
                                 String minDateRetentionPeriod, String inEffectDate, String dateGuidanceReviewed,
                                 String endDate) {
        this.name = name;
        this.description = description;
        this.sort = sort;
        this.url = url;
        this.engine = engine;
        this.minDataRetentionPeriod = minDateRetentionPeriod;
        this.inEffectDate = inEffectDate;
        this.dateGuidanceReviewed = dateGuidanceReviewed;
        this.endDate = endDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getDateGuidanceReviewed() {
        return dateGuidanceReviewed;
    }

    public void setDateGuidanceReviewed(String dateGuidanceReviewed) {
        this.dateGuidanceReviewed = dateGuidanceReviewed;
    }

    public String getInEffectDate() {
        return inEffectDate;
    }

    public void setInEffectDate(String inEffectDate) {
        this.inEffectDate = inEffectDate;
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

    public String getMinDataRetentionPeriod() {
        return minDataRetentionPeriod;
    }

    public void setMinDataRetentionPeriod(String minDataRetentionPeriod) {
        this.minDataRetentionPeriod = minDataRetentionPeriod;
    }

    public String getEngine() {
        return engine;
    }

    public void setEngine(String engine) {
        this.engine = engine;
    }
}
