package com.exam.ideaforge.repository;

import com.exam.ideaforge.entity.IdeaCategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IdeaCategoryRepository extends JpaRepository<IdeaCategoryEntity, UUID> {

    List<IdeaCategoryEntity> findByEnabledTrueOrderBySortOrderAscCodeAsc();

    List<IdeaCategoryEntity> findAllByOrderBySortOrderAscCodeAsc();

    Optional<IdeaCategoryEntity> findByCode(String code);

    Optional<IdeaCategoryEntity> findByCodeAndEnabledTrue(String code);

    boolean existsByCode(String code);
}
