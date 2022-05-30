package me.sample.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import me.sample.domain.Terminal;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

@Repository
public interface TerminalRepository extends JpaRepository<Terminal, UUID>, JpaSpecificationExecutor<Terminal> {

    Long countByTest(Boolean test);

    @Query("SELECT terminal FROM Terminal terminal")
    Stream<Terminal> findAsStreamAll();

    Optional<Terminal> findFirstByHardwareIdAndApplicationApiKeyOrderByUpdatedDateDesc(String hardwareId, String applicationApiKey);
}
