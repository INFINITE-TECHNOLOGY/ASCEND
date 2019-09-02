package io.infinite.ascend.config.entities.enums

/**
 * Bug in Hibernate. https://github.com/spring-projects/spring-boot/issues/14344
 * Can't use Enums with converter.
 */
class HttpMethod {

    final String GET = "GET"
    final String HEAD = "HEAD"
    final String POST = "POST"
    final String PUT = "PUT"
    final String PATCH = "PATCH"
    final String DELETE = "DELETE"
    final String OPTIONS = "OPTIONS"
    final String TRACE = "TRACE"

}