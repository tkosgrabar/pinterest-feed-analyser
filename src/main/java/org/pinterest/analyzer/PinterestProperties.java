package org.pinterest.analyzer;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "pinterest", ignoreUnknownFields = false)
public class PinterestProperties {

    @NotBlank
    private String loginUrl;

    @NotBlank
    private String email;

    @NotBlank
    private String password;

}
