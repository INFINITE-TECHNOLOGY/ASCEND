package io.infinite.ascend.config.entities.converters

import io.infinite.ascend.config.entities.enums.HttpMethod

import javax.persistence.AttributeConverter
import javax.persistence.Converter

@Converter(autoApply = true)
class HttpMethodConverter implements AttributeConverter<HttpMethod, String> {

    @Override
    String convertToDatabaseColumn(HttpMethod httpMethod) {
        if (httpMethod == null) {
            return null
        }
        return httpMethod.stringValue()
    }

    @Override
    HttpMethod convertToEntityAttribute(String httpMethodString) {
        if (httpMethodString == null) {
            return null
        }
        return HttpMethod.values().find {httpMethodString == it.stringValue() }
    }
}