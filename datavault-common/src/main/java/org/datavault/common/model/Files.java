package org.datavault.common.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.HashMap;
import java.util.Map;

/**
 * User: Robin Taylor
 * Date: 01/05/2015
 * Time: 13:51
 *
 * Really not sure if this belongs in the Model, but its here for now.
 *
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class Files {

    private Map filesMap = new HashMap<String, String>();

    public Map getFilesMap() {
        return filesMap;
    }

    public void setFilesMap(Map filesMap) {
        this.filesMap = filesMap;
    }


}
