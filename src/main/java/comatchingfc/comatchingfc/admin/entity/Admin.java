package comatchingfc.comatchingfc.admin.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Admin {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "admin_id")
    private Long id;

    @Column(columnDefinition = "BINARY(16)")
    private byte[] uuid;

    private String username;

    private String accountId;

    private String password;

    @Builder
    public Admin(byte[] uuid, String username, String accountId, String password) {
        this.uuid = uuid;
        this.username = username;
        this.accountId = accountId;
        this.password = password;
    }
}
