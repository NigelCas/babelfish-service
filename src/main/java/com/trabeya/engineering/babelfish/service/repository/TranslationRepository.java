package com.trabeya.engineering.babelfish.service.repository;

import com.trabeya.engineering.babelfish.service.model.TranslationModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TranslationRepository extends JpaRepository<TranslationModel, Long> {

}