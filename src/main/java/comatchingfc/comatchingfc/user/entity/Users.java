package comatchingfc.comatchingfc.user.entity;

import comatchingfc.comatchingfc.survey.response.entity.SurveyResponse;
import comatchingfc.comatchingfc.user.enums.UserRole;
import comatchingfc.comatchingfc.utils.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Users extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "user_ai_info_id", unique = true)
    private UserAiInfo userAiInfo;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SurveyResponse> surveyResponses = new ArrayList<>();

    private String identifyKey;

    private String username;

    private int age;

    private String socialId;

    private String cheeringPlayer;

    @Enumerated(EnumType.STRING)
    private UserRole role = UserRole.ROLE_PENDING;

    private Boolean deactivated = false;

    @Builder
    public Users(String identifyKey, String username, int age, String socialId, String cheeringPlayer) {
        this.identifyKey = identifyKey;
        this.username = username;
        this.age = age;
        this.socialId = socialId;
        this.cheeringPlayer = cheeringPlayer;
    }

    public void setUserAiInfo(UserAiInfo userAiInfo) {
        this.userAiInfo = userAiInfo;
        if (userAiInfo.getUsers() != this) {
            userAiInfo.setUsers(this);
        }
    }

    public void addSurveyResponse(SurveyResponse surveyResponse) {
        surveyResponses.add(surveyResponse);
        if (surveyResponse.getUser() != this) {
            surveyResponse.setUser(this);
        }
    }

    public void removeSurveyResponse(SurveyResponse surveyResponse) {
        surveyResponses.remove(surveyResponse);
        if (surveyResponse.getUser() == this) {
            surveyResponse.setUser(null);
        }
    }

    public void updateUsername(String username) {
        this.username = username;
    }

    public void updateAge(int age) {
        this.age = age;
    }

    public void setRoleToUser() {
        this.role = UserRole.ROLE_USER;
    }

    public void deactivateUser() {
        this.deactivated = true;
    }

    public void activateUser() {
        this.deactivated = false;
    }
}
