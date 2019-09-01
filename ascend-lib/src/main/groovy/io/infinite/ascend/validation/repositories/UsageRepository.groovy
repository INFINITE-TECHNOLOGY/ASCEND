package io.infinite.ascend.validation.repositories


import io.infinite.ascend.validation.entities.Usage

interface UsageRepository {

    Set<Usage> findByAuthorizationId(UUID authorizationId)

    void saveAndFlush(Usage usage)

}
