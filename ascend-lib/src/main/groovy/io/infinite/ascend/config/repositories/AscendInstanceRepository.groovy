package io.infinite.ascend.config.repositories

import io.infinite.ascend.config.entities.AscendInstance
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.rest.core.annotation.RepositoryRestResource

@RepositoryRestResource
interface AscendInstanceRepository extends JpaRepository<AscendInstance, Long> {

    @Query("""select a from AscendInstance a
        where a.id = 1""")
    AscendInstance getAscendInfo()

}
