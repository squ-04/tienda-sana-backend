package co.uniquindio.tiendasana.model.documents;

import co.uniquindio.tiendasana.model.enums.AccountStatus;
import co.uniquindio.tiendasana.model.enums.Role;
import co.uniquindio.tiendasana.model.vo.User;
import co.uniquindio.tiendasana.model.vo.ValidationCode;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document("accounts")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Account {
    //Main attribute for identification
    @Id
    @EqualsAndHashCode.Include
    private String id;
    //Secundary attributes for the class
    private User user;
    private String email;
    private String password;
    private AccountStatus status;
    private Role role;
    private LocalDateTime registrationDate;

    //Attributes are for security on the accounts
    private ValidationCode verificationCode;
    private ValidationCode passwordValidation;

    @Builder
    private Account (User user, String email, String password, Role role, LocalDateTime registrationDate,
                     AccountStatus status,ValidationCode verificationCode, ValidationCode passwordValidation) {
        this.user = user;
        this.email = email;
        this.password = password;
        this.role = role;
        this.registrationDate = registrationDate;
        this.status = status;
        this.verificationCode = verificationCode;
        this.passwordValidation = passwordValidation;

    }
}