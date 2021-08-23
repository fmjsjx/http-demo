package com.github.fmjsjx.demo.http;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * SSL properties class.
 */
@Getter
@Setter
@ToString
public class SslProperties {

    /**
     * Whether to enable SSL support. The default is false.
     */
    private boolean enabled = false;

    /**
     * Path string of the key certificate chain file. The file must be an X.509
     * certificate chain file in PEM format.
     */
    private String keyCertChainFile;

    /**
     * Path string of the key file. The file must be a PKCS#8 private key file in
     * PEM format.
     */
    private String keyFile;

    /**
     * The password of the {@code keyFile}, or {@code null} if it's not
     * password-protected.
     */
    private String keyPassword;

}
