package com.antock.api.coseller.infrastructure;

import com.antock.api.coseller.domain.CorpMast;
import com.antock.api.dashboard.application.dto.RegionStatDto;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CorpMastRepositoryCustom {
    List<RegionStatDto> getRegionStats();
}