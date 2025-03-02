package co.uniquindio.tiendasana.model.vo;

import lombok.*;

import java.time.LocalDateTime;

@Setter
@Getter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ValidationCode {


    //Attributes for the class
    @EqualsAndHashCode.Include
    private String code;
    private LocalDateTime creationDate;

    //Constructor method for the class
    @Builder
    private ValidationCode(String code, LocalDateTime creationDate) {
        this.code = code;
        this.creationDate = creationDate;
    }
}