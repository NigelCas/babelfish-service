package com.trabeya.engineering.babelfish.service.repository;

import com.trabeya.engineering.babelfish.service.model.TranslationRequest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TranslationRepository extends JpaRepository<TranslationRequest, Long> {

}