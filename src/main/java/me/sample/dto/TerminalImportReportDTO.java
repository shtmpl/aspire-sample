package me.sample.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Objects;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TerminalImportReportDTO {
    Integer newCount;
    Integer updateCount;

    public Integer newIncrease() {
        return Objects.isNull(newCount) ? newCount = 1 : newCount++;
    }

    public Integer updateIncrease() {
        return Objects.isNull(updateCount) ? updateCount = 1 : updateCount++;
    }
}
