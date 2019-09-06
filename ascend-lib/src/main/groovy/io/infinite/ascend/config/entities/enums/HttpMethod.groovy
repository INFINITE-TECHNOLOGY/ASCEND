package io.infinite.ascend.config.entities.enums

/**
 * Bug in Hibernate. https://github.com/spring-projects/spring-boot/issues/14344
 * Can't use Enums with converter.
 */
class HttpMethod {

    static final String GET = "GET"
    static final String HEAD = "HEAD"
    static final String POST = "POST"
    static final String PUT = "PUT"
    static final String PATCH = "PATCH"
    static final String DELETE = "DELETE"
    static final String OPTIONS = "OPTIONS"
    static final String TRACE = "TRACE"

}