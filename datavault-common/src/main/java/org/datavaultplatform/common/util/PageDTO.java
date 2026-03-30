package org.datavaultplatform.common.util;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

@Data
@NoArgsConstructor
public class PageDTO<T> {

    private int totalPages;
    private long totalElements;
    private List<T> content;
    
    public PageDTO(Page<T> page) {
        this.totalPages = page.getTotalPages();
        this.totalElements = page.getTotalElements();
        this.content = page.getContent();
    }
}
