package io.infinite.ascend.config.repositories


import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.rest.core.annotation.RepositoryRestResource

@RepositoryRestResource
interface AscendInstance extends JpaRepository<io.infinite.ascend.config.entities.AscendInstance, Long> {

    @Query("""select a from AscendInstance a
        where a.id = 1""")
    io.infinite.ascend.config.entities.AscendInstance getAscendInfo()

}
