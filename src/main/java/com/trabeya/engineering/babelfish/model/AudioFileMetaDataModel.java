package com.trabeya.engineering.babelfish.model;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "audio_metadata")
public class AudioFileMetaDataModel {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "author")
    private String author;

    @Column(name = "channels")
    private String channels;

    @Column(name = "encoder_version")
    private String encoderVersion;

    @Column(name = "title")
    private String title;

    @Column(name = "sample_rate")
    private String sampleRate;

    @Column(name = "content_type")
    private String contentType;
}
