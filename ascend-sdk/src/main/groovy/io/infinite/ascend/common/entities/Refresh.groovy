package io.infinite.ascend.common.entities

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonIgnore
import groovy.transform.ToString
import org.hibernate.annotations.Type

import javax.persistence.*

@Entity
@ToString(includeNames = true, includeFields = true, excludes = ["jwt"])
class Refresh {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    Long id

    UUID guid = UUID.randomUUID()

    String name

    String identityName

    String serverNamespace

    String clientNamespace

    String scopeName

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.PERSIST)
    @OrderColumn
    @JoinTable
    Set<Scope> scopes = new HashSet<Scope>()

    @ElementCollection(fetch = FetchType.EAGER)
    Map<String, String> refreshCredentials = new HashMap<String, String>()

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
