package com.trabeya.engineering.babelfish.repository;

import com.trabeya.engineering.babelfish.model.SpeechToTextTranscription;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpeechToTextTranscriptionRepository extends JpaRepository<SpeechToTextTranscription, Long> {

}
