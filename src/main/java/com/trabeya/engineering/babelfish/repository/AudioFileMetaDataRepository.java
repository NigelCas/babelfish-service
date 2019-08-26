package com.trabeya.engineering.babelfish.repository;

import com.trabeya.engineering.babelfish.model.AudioFileMetaData;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AudioFileMetaDataRepository extends JpaRepository<AudioFileMetaData, Long> {
}
