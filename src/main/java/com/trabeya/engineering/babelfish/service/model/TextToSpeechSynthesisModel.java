package com.trabeya.engineering.babelfish.service.model;

import com.google.cloud.texttospeech.v1.AudioEncoding;
import com.google.cloud.texttospeech.v1.SsmlVoiceGender;
import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "text_to_speech_synthesis")
public class TextToSpeechSynthesisModel {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private  Long id;

    @Column(name = "input_text")
    private String inputText;

    @Column(name = "voice_language_code")
    private String voiceLanguageCode;

    @Column(name = "voice_gender")
    @Enumerated(EnumType.STRING)
    private SsmlVoiceGender gender;

    @Column(name = "audio_config_encoding")
    @Enumerated(EnumType.STRING)
    private AudioEncoding audioEncoding;

    @Column(name = "synthesis_status")
    @Enumerated(EnumType.STRING)
    private Status status;

}
