package org.pinterest.analyzer.controller;

import org.pinterest.analyzer.service.CrawlerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static java.util.stream.Collectors.toList;

@RestController
@RequestMapping(value = "/api")
public class AnalysisController {

    private final CrawlerService crawlerService;

    @Autowired
    public AnalysisController(CrawlerService crawlerService) {
        this.crawlerService = crawlerService;
    }

    @RequestMapping(value = "/analyze", method = RequestMethod.POST)
    public AnalysisResponse analyze(@RequestBody AnalysisRequest request) {
        List<Analysis> analysis = crawlerService.crawl(request)
                .entrySet().stream()
                .map(e -> new Analysis(e.getKey(), e.getValue()))
                .sorted((a1, a2) -> Integer.compare(a2.getCount(), a1.getCount()))
                .collect(toList());

        return new AnalysisResponse(analysis);
    }
}
