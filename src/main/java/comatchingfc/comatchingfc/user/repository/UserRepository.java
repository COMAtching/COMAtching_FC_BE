package comatchingfc.comatchingfc.user.repository;

import comatchingfc.comatchingfc.user.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<Users, Long> {

    Optional<Users> findByIdentifyKey(String identifyKey);

    @Query("SELECT u FROM Users u JOIN u.userAiInfo uf WHERE uf.uuid = :uuid")
    Optional<Users> findUsersByUuid(@Param("uuid") byte[] uuid);
}
