package com.trabeya.engineering.babelfish.model;

import lombok.Data;

import javax.persistence.*;
import java.time.LocalTime;

@Data
@Entity
@Table(name = "audio_metadata")
public class AudioFileMetaData {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "channels")
    private String channels;

    @Column(name = "sample_rate")
    private String sampleRateHz;

    @Column(name = "bit_rate")
    private String bitRateKbps;

    @Column(name = "content_type")
    private String contentType;

    @Basic
    @Column(name = "audio_length")
    private LocalTime trackLengthIso;
}
