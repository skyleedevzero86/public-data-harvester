package com.antock.api.coseller.infrastructure;

import com.antock.api.coseller.domain.CorpMast;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface CorpMastRepository extends JpaRepository<CorpMast, Long> {

    boolean existsByBizNo(String bizNo);

    Optional<CorpMast> findByBizNo(@Param("bizNo") String bizNo);

}