package co.uniquindio.tiendasana.model.vo;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class User {
    private String dni;
    private String name;
    private String address;
    private String phone;

    @Builder
    private User (String dni, String name, String address, String phone) {
        this.dni = dni;
        this.name = name;
        this.address = address;
        this.phone = phone;
    }
}
