package com.github.fmjsjx.demo.http;

import java.net.InetAddress;
import java.time.Duration;

import org.springframework.util.unit.DataSize;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class HttpProperties {

    /**
     * Server HTTP port.
     */
    private int port = 8080;

    /**
     * Network address to which the server should bind.
     */
    private InetAddress address;

    /**
     * The number of maximum length for HTTP body content.
     * <p>
     * The default value is 1M.
     */
    private DataSize maxContentSize = DataSize.ofMegabytes(1);

    /**
     * Time that connectors wait for another request before closing the connection.
     * <p>
     * The default value is 60s.
     */
    private Duration timeout = Duration.ofSeconds(60);

}
