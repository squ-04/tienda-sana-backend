package co.uniquindio.tiendasana.model.vo;

import lombok.*;

import java.time.LocalDateTime;

@Setter
@Getter
@NoArgsConstructor
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Pago {
    @EqualsAndHashCode.Include
    private String id;

    private String currency;
    private String paymentType;
    private String statusDetail;
    private String authorizationCode;
    private LocalDateTime date;
    private float transactionValue;
    private String status;

    @Builder
    public Pago(String id, String currency, String paymentType, String statusDetail,
                String authorizationCode, LocalDateTime date, float transactionValue, String status) {
        this.id = id;
        this.currency = currency;
        this.paymentType = paymentType;
        this.statusDetail = statusDetail;
        this.authorizationCode = authorizationCode;
        this.date = date;
        this.transactionValue = transactionValue;
        this.status = status;
    }
}
