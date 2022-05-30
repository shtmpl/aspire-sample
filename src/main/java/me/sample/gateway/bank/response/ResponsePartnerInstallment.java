package me.sample.gateway.bank.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class ResponsePartnerInstallment {

    /**
     * promo.period - Максимальный период акционной рассрочки партнера
     */
    ResponseInstallmentSegment promo;

    /**
     * tariff.period - Максимальный период относительной рассрочки
     */
    ResponseInstallmentSegment tariff;

    /**
     * basic.period - Максимальный период базовой рассрочки
     */
    @JsonProperty("default")
    ResponseInstallmentSegment basic;
}
