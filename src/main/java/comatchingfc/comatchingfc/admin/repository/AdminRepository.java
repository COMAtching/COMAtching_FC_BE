package comatchingfc.comatchingfc.admin.repository;

import comatchingfc.comatchingfc.admin.entity.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AdminRepository extends JpaRepository<Admin, Long> {
    Boolean existsAdminByAccountId(String accountId);

    Optional<Admin> findByAccountId(String accountId);

    Optional<Admin> findByUuid(byte[] uuid);
}
