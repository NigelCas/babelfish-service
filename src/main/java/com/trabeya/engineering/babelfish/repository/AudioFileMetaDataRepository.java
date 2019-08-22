package com.trabeya.engineering.babelfish.repository;

import com.trabeya.engineering.babelfish.model.AudioFileMetaDataModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AudioFileMetaDataRepository extends JpaRepository<AudioFileMetaDataModel, Long> {
}
