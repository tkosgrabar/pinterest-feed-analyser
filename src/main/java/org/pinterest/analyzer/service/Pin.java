package org.pinterest.analyzer.service;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Pin {

    private String imgUrl;
    private String pinUrl;
    private String description;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Pin pin = (Pin) o;

        return imgUrl != null ? imgUrl.equals(pin.imgUrl) : pin.imgUrl == null;

    }

    @Override
    public int hashCode() {
        return imgUrl != null ? imgUrl.hashCode() : 0;
    }
}
