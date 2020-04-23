package io.infinite.ascend.common.entities

import com.fasterxml.jackson.annotation.JsonIgnore
import groovy.transform.ToString
import org.hibernate.annotations.Type

import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Lob

@Entity
@ToString(includeNames = true, includeFields = true)
class Grant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    Long id

    @Lob
    @Type(type = "org.hibernate.type.TextType")
    String urlRegex

    @Lob
    @Type(type = "org.hibernate.type.TextType")
    String bodyRegex

    String httpMethod

}