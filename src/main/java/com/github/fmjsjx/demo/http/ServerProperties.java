package com.github.fmjsjx.demo.http;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ConfigurationProperties("server")
public class ServerProperties {

    /**
     * Number of I/O threads to create for the worker. The default is derived from
     * the number of available processors.
     */
    private Integer ioThreads;

    @NestedConfigurationProperty
    private SslProperties ssl;

    @NestedConfigurationProperty
    private HttpProperties http;

    /**
     * The mode of server.
     * <p>
     * The default value is {@code secondary}.
     */
    private Mode mode = Mode.SECONDARY;

    private HTTPClientMode httpClientMode = HTTPClientMode.DEFAULT;

    /**
     * Server mode Enumeration class.
     */
    public enum Mode {

        PRIMARY, SECONDARY;

    }

    public enum HTTPClientMode {
        DEFAULT, SIMPLE
    }

}
