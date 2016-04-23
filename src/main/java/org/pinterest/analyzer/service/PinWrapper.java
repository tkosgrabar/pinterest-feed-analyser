package org.pinterest.analyzer.service;

import lombok.*;

@Getter
@Setter
@ToString
@EqualsAndHashCode
public class PinWrapper extends Pin {

    private final String top;
    private final String left;

    public PinWrapper(String imgUrl, String pinUrl, String description, String top, String left) {
        super(imgUrl, pinUrl, description);
        this.top = top;
        this.left = left;
    }
}
