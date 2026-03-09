package org.datavaultplatform.common.request;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSetter;
import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(name = "CreateRetentionPolicy")
@Data
public class CreateRetentionPolicy {

    @Schema(description = "Uh... the id")
    private int id;

    @Schema(description = "A name for the new Retention Policy")
    private String name;

    @Schema(description = "A description of Retention Policy")
    private String description;

    @Schema(description = "Engine for Retention Policy (deprecated")
    private String engine;

    @Schema(description = "Sorting order for Retention Policy (deprecated)")
    private String sort;

    @Schema(description = "URL for Retention Policy")
    private String url;

    @Schema(description = "Minimum retention period in years")
    private int minRetentionPeriod;

    @Schema(description = "Extend the expiry date of a vault if a deposit is retrieved")
    private boolean extendUponRetrieval;

    @Schema(description = "Minimum Date Retention Period (deprecated)")
    private String minDataRetentionPeriod;

    @Schema(description = "In Effect Date")
    private LocalDate inEffectDate;

    @Schema(description = "End Date")
    private LocalDate endDate;

    @Schema(description = "Date Guidance Reviewed")
    private LocalDate dataGuidanceReviewed;

    @Schema(description = "Form Action")
    private String action;

    public CreateRetentionPolicy() { }

    @JsonGetter
    public int getId() {
        return id;
    }
    public int getID() {
        return getId();
    }

    @JsonSetter
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

    public LocalDate getInEffectDate() {
        return inEffectDate;
    }

    public void setInEffectDate(LocalDate inEffectDate) {
        this.inEffectDate = inEffectDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public LocalDate getDataGuidanceReviewed() {
        return dataGuidanceReviewed;
    }

    public void setDataGuidanceReviewed(LocalDate dataGuidanceReviewed) {
        this.dataGuidanceReviewed = dataGuidanceReviewed;
    }

    public String getPolicyInfo() {
        String retVal = this.id + "-" + this.minRetentionPeriod;
        return retVal;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }
}
