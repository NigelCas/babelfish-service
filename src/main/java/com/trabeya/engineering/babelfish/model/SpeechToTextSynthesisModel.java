package com.trabeya.engineering.babelfish.model;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

public class SpeechToTextSynthesisModel {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String filename;

    private String fileUri;

    private String outputTranscrption;

}
