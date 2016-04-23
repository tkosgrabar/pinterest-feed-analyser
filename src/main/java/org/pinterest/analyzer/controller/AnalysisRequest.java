package org.pinterest.analyzer.controller;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AnalysisRequest {

    private String url;
    private Integer count;
    private String filter;

}
