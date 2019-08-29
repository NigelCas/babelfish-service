package com.trabeya.engineering.babelfish.model;

import com.google.cloud.texttospeech.v1.AudioEncoding;
import com.google.cloud.texttospeech.v1.SsmlVoiceGender;
import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "text_to_speech_synthesis")
public class TextToSpeechSynthesis {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private  Long id;

    @Lob
    @Column(name = "input_data", length=512)
    private String inputData;

    @Column(name = "input_data_type")
    @Enumerated(EnumType.STRING)
    private TextToSpeechTextType inputDataType;

    @Column(name = "remote_model")
    private String remoteModelFilename;

    @Column(name = "remote_model_uri")
    private String remoteModelUri;

    @Column(name = "output_audio")
    private String audioFilename;

    @OneToOne
    @JoinTable(name = "output_audio_metadata", joinColumns = {
            @JoinColumn(name = "synthesis_id", referencedColumnName = "id")}, inverseJoinColumns = {
            @JoinColumn(name = "metadata_id", referencedColumnName = "id")})
    private AudioFileMetaData detectedAudioMetaData;

    @Column(name = "output_audio_uri")
    private String audioFileUri;

    @Column(name = "voice_language_name")
    private String voiceLanguageName;

    @Column(name = "voice_language_code")
    private String voiceLanguageCode;

    @Column(name = "voice_gender")
    @Enumerated(EnumType.STRING)
    private SsmlVoiceGender voiceGender;

    @Column(name = "audio_config_encoding")
    @Enumerated(EnumType.STRING)
    private AudioEncoding audioEncoding;

    @Column(name = "audio_config_device_profile")
    @Enumerated(EnumType.STRING)
    private TextToSpeechSynthesisDeviceProfile audioDeviceProfile;

    @Column(name = "synthesis_status")
    @Enumerated(EnumType.STRING)
    private Status status;

    @Version
    @Column(name = "version")
    private int version;

}
