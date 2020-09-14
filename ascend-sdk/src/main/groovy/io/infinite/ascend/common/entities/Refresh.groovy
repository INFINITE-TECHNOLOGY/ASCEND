package io.infinite.ascend.common.entities

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonInclude
import groovy.transform.ToString
import org.hibernate.annotations.Type

import javax.persistence.*

@Entity
@JsonInclude(JsonInclude.Include.NON_NULL)
@ToString(includeNames = true, includeFields = true, excludes = ["jwt"])
class Refresh {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    Long id

    @Column(unique = true)
    UUID guid = UUID.randomUUID()

    UUID authorizationGuid

    String authorizationName

    String serverNamespace

    String clientNamespace

    Integer durationSeconds

    Integer maxUsageCount

    @JsonFormat(timezone = "UTC", pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    Date creationDate

    @JsonFormat(timezone = "UTC", pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    Date expiryDate

    @Lob
    @Type(type = "org.hibernate.type.TextType")
    String jwt

}
