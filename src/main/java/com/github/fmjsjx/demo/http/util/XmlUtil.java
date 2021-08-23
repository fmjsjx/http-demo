package com.github.fmjsjx.demo.http.util;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class XmlUtil {

    private static final XmlMapper MAPPER;

    static {
        var mapper = new XmlMapper();
        mapper.setSerializationInclusion(Include.NON_ABSENT);
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        mapper.registerModules(new Jdk8Module(), new JavaTimeModule());
        MAPPER = mapper;
    }

    public static final String dumps(Object value) throws XmlException {
        try {
            return MAPPER.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new XmlException(e);
        }
    }

    public static final <T> T loads(String src, Class<T> type) throws XmlException {
        try {
            return MAPPER.readValue(src, type);
        } catch (JsonProcessingException e) {
            throw new XmlException(e);
        }
    }

    public static final <T> T loads(String src, JavaType type) throws XmlException {
        try {
            return MAPPER.readValue(src, type);
        } catch (JsonProcessingException e) {
            throw new XmlException(e);
        }
    }

    public static final <T> T loads(String src, TypeReference<T> typeRef) throws XmlException {
        try {
            return MAPPER.readValue(src, typeRef);
        } catch (JsonProcessingException e) {
            throw new XmlException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public static final <T extends JsonNode> T loads(String src) throws XmlException {
        try {
            return (T) MAPPER.readTree(src);
        } catch (JsonProcessingException e) {
            throw new XmlException(e);
        }
    }

    public static final class XmlException extends RuntimeException {

        private static final long serialVersionUID = 2074679298886601481L;

        public XmlException(String message, Throwable cause) {
            super(message, cause);
        }

        public XmlException(Throwable cause) {
            this(cause.getMessage(), cause);
        }

    }

    private XmlUtil() {
    }

}
