package io.infinite.ascend.config.repositories

import io.infinite.ascend.config.entities.AscendInfo
import io.infinite.ascend.config.entities.AuthorizationType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.rest.core.annotation.RepositoryRestResource

@RepositoryRestResource
interface AscendInfoRepository extends JpaRepository<AscendInfo, Long> {

    @Query("""select a from AscendInfo a
        where a.id = 1""")
    AscendInfo getAscendInfo()

}
