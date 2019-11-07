package com.trabeya.engineering.babelfish.model;

import com.google.cloud.speech.v1.RecognitionConfig.AudioEncoding;
import lombok.Data;
import org.hibernate.annotations.Type;

import javax.persistence.*;

@Data
@Entity
@Table(name = "speech_to_text_transcription")
public class SpeechToTextTranscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "remote_model")
    private String remoteModelFilename;

    @Column(name = "remote_model_uri")
    private String remoteModelUri;

    @Column(name = "audio_file_name")
    private String inputFilename;

    @Column(name = "audio_file_uri")
    private String inputFileUri;

    @Column(name = "audio_language_code")
    private String audioLanguageCode;

    @Column(name = "audio_config_encoding")
    @Enumerated(EnumType.STRING)
    private AudioEncoding audioEncoding;

    @Column(name = "audio_sample_rate")
    private int sampleRate;

    @Column(name = "speech_context_hints")
    private String speechContextsHints;

    @Column(name = "audio_streaming_model")
    @Enumerated(EnumType.STRING)
    private SpeechToTextStreamingModel audioStreamingModel;

    @Column(name = "separate_channel_recognition_enabled")
    private boolean enableSeparateRecognitionPerChannel;

    @Column(name = "profanity_filter_enabled")
    private boolean isProfanityFilterEnabled;

    @Column(name = "enhanced_model_enabled")
    private boolean isEnhancedEnabled;

    @OneToOne
    @JoinTable(name = "input_audio_metadata", joinColumns = {
            @JoinColumn(name = "transcription_id", referencedColumnName = "id")}, inverseJoinColumns = {
            @JoinColumn(name = "metadata_id", referencedColumnName = "id")})
    private AudioFileMetaData detectedAudioMetaData;

    @Lob
    @Type(type="text")
    @Column(name = "output_transcription", columnDefinition="CLOB")
    private String outputTranscription;

    @Column(name = "transcription_status")
    @Enumerated(EnumType.STRING)
    private Status status;

    @Version
    @Column(name = "version")
    private int version;

}
