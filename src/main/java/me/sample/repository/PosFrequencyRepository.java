package me.sample.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import me.sample.domain.PosFrequency;

import java.time.LocalDateTime;
import java.util.List;

public interface PosFrequencyRepository extends Repository<PosFrequency, String> {
    @Query(value = "select count(*)               as count, " +
            "       md5(cast(random() as text))   as id, " +
            "       date_part('hour', lastTs.ts)  as hour, " +
            "       date_part('hour', lastTs.ts)  as hour, " +
            "       date_part('day', lastTs.ts)   as day, " +
            "       null                          as week, " +
            "       date_part('month', lastTs.ts) as month, " +
            "       date_part('year', lastTs.ts)  as year " +
            "from (select max(gpi.cdat) as ts " +
            "      from geo_pos_info gpi " +
            "      where gpi.cdat >= :from " +
            "        and gpi.cdat <= :to " +
            "      group by gpi.terminal_id) lastTs " +
            "group by hour, day, month, year " +
            "order by year, month, day, hour", nativeQuery = true)
    List<PosFrequency> perHour(LocalDateTime from, LocalDateTime to);

    @Query(value = "select count(*)               as count, " +
            "       md5(cast(random() as text))   as id, " +
            "       null                          as hour, " +
            "       date_part('day', lastTs.ts)   as day, " +
            "       null                          as week, " +
            "       date_part('month', lastTs.ts) as month, " +
            "       date_part('year', lastTs.ts)  as year " +
            "from (select max(gpi.cdat) as ts " +
            "      from geo_pos_info gpi " +
            "      where gpi.cdat >= :from " +
            "        and gpi.cdat <= :to " +
            "      group by gpi.terminal_id) lastTs " +
            "group by day, month, year " +
            "order by year, month, day", nativeQuery = true)
    List<PosFrequency> perDay(LocalDateTime from, LocalDateTime to);

    @Query(value = "select count(*)               as count, " +
            "       md5(cast(random() as text))   as id, " +
            "       null                          as hour, " +
            "       null                          as day, " +
            "       date_part('week', lastTs.ts)  as week, " +
            "       date_part('month', lastTs.ts) as month, " +
            "       date_part('year', lastTs.ts)  as year " +
            "from (select max(gpi.cdat) as ts " +
            "      from geo_pos_info gpi " +
            "      where gpi.cdat >= :from " +
            "        and gpi.cdat <= :to " +
            "      group by gpi.terminal_id) lastTs " +
            "group by week, month, year " +
            "order by year, month, week", nativeQuery = true)
    List<PosFrequency> perWeek(LocalDateTime from, LocalDateTime to);

    @Query(value = "select count(*)               as count, " +
            "       md5(cast(random() as text))   as id, " +
            "       null                          as hour, " +
            "       null                          as day, " +
            "       null                          as week, " +
            "       date_part('month', lastTs.ts) as month, " +
            "       date_part('year', lastTs.ts)  as year " +
            "from (select max(gpi.cdat) as ts " +
            "      from geo_pos_info gpi " +
            "      where gpi.cdat >= :from " +
            "        and gpi.cdat <= :to " +
            "      group by gpi.terminal_id) lastTs " +
            "group by month, year " +
            "order by year, month", nativeQuery = true)
    List<PosFrequency> perMonth(LocalDateTime from, LocalDateTime to);

}
