package org.pinterest.analyzer.controller;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class AnalysisResponse {

    private final List<Analysis> analysis;

}
