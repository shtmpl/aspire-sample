package me.sample.gateway.bank.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.UUID;

@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class ResponseShop {

    /**
     * Id магазина
     */
    UUID id;

    /**
     * Id партнера
     */
    UUID partnerId;

    /**
     * Массив номеров телефона магазина.
     * В случае если нет номеров, то пустой массив
     */
    List<String> phones;

    /**
     * Код КЛАДР
     */
    String kladrCode;

    /**
     * Код ФИАС
     */
    String fiasCode;

    /**
     * Массив id акций, в которые входит торговая точка
     */
    List<UUID> promos;

    ResponseShopLocation location;

    ResponseShopInstallment installment;
}
