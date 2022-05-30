package me.sample.domain;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public enum NotificationState {

    /**
     * Уведомление создано
     */
    CREATED,

    /**
     * Уведомление запланировано для обработки (планировщиком)
     */
    SCHEDULED,


    /**
     * Уведомление находится в очереди на отправку на сервер
     */
    QUEUED,


    /**
     * Уведомпление принято сервером
     */
    ACCEPTED_BY_SERVER,

    /**
     * Уведомление отклонено сервером
     */
    REJECTED_BY_SERVER,


    /**
     * Уведомление получено клиентом
     */
    RECEIVED_BY_CLIENT,

    /**
     * Уведомление просмотрено клиентом
     */
    ACKNOWLEDGED_BY_CLIENT,


    /**
     * Уведомление обработано неудачно (на каком-либо этапе)
     */
    FAILED
}
