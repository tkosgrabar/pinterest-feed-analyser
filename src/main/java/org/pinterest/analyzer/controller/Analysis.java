package org.pinterest.analyzer.controller;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.pinterest.analyzer.service.Pin;

@Getter
@Setter
@AllArgsConstructor
public class Analysis {

    private final Pin pin;
    private final Integer count;
}
