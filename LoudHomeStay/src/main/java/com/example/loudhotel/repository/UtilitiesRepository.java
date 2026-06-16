package com.example.loudhotel.repository;

import com.example.loudhotel.entity.Utilities;
import com.example.loudhotel.enums.UtilityType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UtilitiesRepository extends JpaRepository<Utilities, Long> {

    Optional<Utilities> findByUtilitiesName(String utilitiesName);

    boolean existsByUtilitiesName(String utilitiesName);

    Page<Utilities> findByUtilitiesNameContainingIgnoreCase(String utilitiesName, Pageable pageable);

    Page<Utilities> findByUtilityType(UtilityType utilityType, Pageable pageable);

    Page<Utilities> findByUtilitiesNameContainingIgnoreCaseAndUtilityType(String utilitiesName, UtilityType utilityType, Pageable pageable);
}