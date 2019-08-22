package com.trabeya.engineering.babelfish.repository;

import com.trabeya.engineering.babelfish.model.TranslationModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TranslationRepository extends JpaRepository<TranslationModel, Long> {

}