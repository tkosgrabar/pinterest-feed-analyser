package org.pinterest.analyzer;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "proxy", ignoreUnknownFields = false)
public class ProxyProperties {

    private String socksProxy;

    private String proxyUsername;

    private String proxyPassword;

}
