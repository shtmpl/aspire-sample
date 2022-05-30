package me.sample.gateway.bank.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class ResponsePartner {

    /**
     * Идентификатор партнера
     */
    UUID id;

    /**
     * Дата создания.
     * Формат: yyyy-MM-dd HH:mm:ss
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime creationDate;


    /**
     * Дата последнего обновления.
     * Дата меняется, если изменены данные партнера, или данные хотя бы одной из его ТТ или акции.
     * Для новосозданной сущности null.
     * Формат: yyyy-MM-dd HH:mm:ss
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime lastUpdateDate;

    /**
     * Признак активности партнера
     */
    Boolean isActive;

    /**
     * Внутреннее наименование партнера
     */
    String title;

    /**
     * Отображаемое наименование партнера
     */
    String name;

    /**
     * Наименование для URL
     */
    String urlName;

    /**
     * Краткое описание партнера
     */
    String descriptionShort;

    /**
     * Описание партнера
     */
    String descriptionFull;

    /**
     * Флаг наличия доставки по всей территории РФ
     */
    Boolean deliveryRussia;

    /**
     * Массив наименований регионов/городов из раздела регионы.
     * В случае, если deliveryRussia = true, то пустой массив
     */
    List<String> deliveryRegions;

    /**
     * Флаг наличия интернет-магазинов
     */
    Boolean hasOnlineStore;

    /**
     * Признак наличия физических магазинов
     */
    Boolean hasOfflineStore;

    /**
     * Флаг оплаты на сайте партнера
     */
    Boolean sitePayment;

    /**
     * Флаг оплаты в приложении партнера
     */
    Boolean appPayment;

    /**
     * Количество городов, в которых есть магазины партнера
     */
    Integer citiesCount;

    /**
     * Массив хештегов.
     * В случае, если хэштэгов нет, то пустой массив
     */
    List<ResponseHashtag> hashtags;

    /**
     * Id главной категории, к которой принадлежит партнер
     */
    String mainCategory;

    /**
     * Массив с id категорий, к которым принадлежит партнер.
     * Массив дублирует id главной категории, к которой принадлежит партнер
     */
    List<UUID> categories;

    /**
     * Наличие акций
     */
    Boolean hasPromo;

    /**
     * Массив id акций, в которые партнер входит полностью.
     */
    List<UUID> promos;

    /**
     * Массив id магазинов, которые привязаны к партнеру
     */
    List<UUID> shopsId;

    List<ResponseImage> images;

    ResponsePartnerInstallment installment;
}
