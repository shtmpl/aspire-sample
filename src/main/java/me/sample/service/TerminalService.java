package me.sample.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import me.sample.dto.TerminalDTO;
import me.sample.domain.Terminal;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface TerminalService {

    long countTerminals(Specification<Terminal> specification);

    Page<Terminal> findTerminals(Pageable pageable);

    Page<Terminal> findTerminals(Specification<Terminal> specification, Pageable pageable);

    List<Terminal> findTerminals(Specification<Terminal> specification);

    Optional<Terminal> findTerminal(UUID id);

    Optional<Terminal> findTerminal(String hardwareId, String applicationApiKey);

    Terminal saveOrUpdateTerminal(TerminalDTO dto);

    CompletableFuture<Terminal> saveOrUpdateTerminalAsync(TerminalDTO dto);

    Terminal saveTerminal(Terminal data);

    Optional<Terminal> updateTerminal(UUID id, Terminal data);

    Terminal updateTerminal(Terminal found, Terminal data);

    Terminal updateTerminalCityByIp(Terminal found, String ip);

    CompletableFuture<Terminal> updateTerminalCityByIpAsync(Terminal found, String ip);

    long syncTerminalCities();

    Terminal syncTerminalCity(Terminal found);

    Optional<UUID> deleteTerminal(UUID uuid);

    // FIXME: Пока не убираем
//    UUID sendTestPush(UUID terminalId, UUID campaignId);
//
//    TerminalImportReportDTO checkImport(List<TerminalDTO> terminalImports);
//
//    void saveImport(List<TerminalDTO> terminalImports);
}
