package io.infinite.ascend.common.entities

import com.fasterxml.jackson.annotation.JsonIgnore
import groovy.transform.ToString
import org.hibernate.annotations.Type

import javax.persistence.*

@Entity
@ToString(includeNames = true, includeFields = true)
class Claim {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    Long id

    Date date = new Date()

    @Lob
    @Type(type = "org.hibernate.type.TextType")
    String incomingUrl

    String method

}
