package io.infinite.ascend.common.entities

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import groovy.transform.ToString
import org.hibernate.annotations.Type

import javax.persistence.*

@Entity
@Table(name = "authorizations")
@ToString(includeNames = true, includeFields = true, excludes = ["jwt"])
class Authorization {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    Long id

    UUID guid = UUID.randomUUID()

    String name

    String serverNamespace

    String clientNamespace

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.PERSIST)
    Identity identity

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.PERSIST)
    Scope scope

    Integer durationSeconds

    Integer maxUsageCount

    @JsonFormat(timezone = "UTC", pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    Date creationDate

    @JsonFormat(timezone = "UTC", pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    Date expiryDate

    @Lob
    @Type(type = "org.hibernate.type.TextType")
    String jwt

    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.PERSIST)
    Authorization prerequisite

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.PERSIST)
    Refresh refresh

    @JsonIgnore
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.PERSIST)
    Set<Claim> claims = new HashSet<Claim>()

    @ElementCollection(fetch = FetchType.EAGER)
    Map<String, String> authorizedCredentials = new HashMap<String, String>()

}
