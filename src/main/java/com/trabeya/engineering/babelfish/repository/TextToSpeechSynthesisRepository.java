package com.trabeya.engineering.babelfish.repository;

import com.trabeya.engineering.babelfish.model.TextToSpeechSynthesisModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TextToSpeechSynthesisRepository extends JpaRepository<TextToSpeechSynthesisModel, Long> {
}
