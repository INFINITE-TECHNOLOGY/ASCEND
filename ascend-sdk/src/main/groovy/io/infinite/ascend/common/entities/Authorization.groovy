package io.infinite.ascend.common.entities

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonIgnore
import groovy.transform.ToString

import javax.persistence.*

@Entity
@ToString(includeNames = true, includeFields = true, excludes = ["jwt", "prerequisiteJwt"])
class Authorization {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    Long id

    UUID guid = UUID.randomUUID()

    String name

    String serverNamespace

    String clientNamespace

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    Identity identity

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    Scope scope

    Integer durationSeconds

    Integer maxUsageCount

    @JsonFormat(timezone = "UTC", pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    Date creationDate

    @JsonFormat(timezone = "UTC", pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    Date expiryDate

    Boolean isSuccessful

    Boolean isAuthenticationFailed

    @Transient
    String jwt

    @Transient
    String prerequisiteJwt

    Boolean isRefresh

    @OneToMany
    Set<Claim> claims = new HashSet<Claim>()

}
