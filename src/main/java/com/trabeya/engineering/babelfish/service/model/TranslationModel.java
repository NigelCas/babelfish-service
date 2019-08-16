package com.trabeya.engineering.babelfish.service.model;

import lombok.Data;
import javax.persistence.*;

@Data
@Entity
@Table(name = "translation")
public class TranslationModel {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private  Long id;

    @Column(name = "input_language")
    private String inputLanguage;

    @Column(name = "output_language")
    private String outputLanguage;

    @Column(name = "translation_format")
    @Enumerated(EnumType.STRING)
    private TranslationOutputFormat outputFormat;

    @Column(name = "input_text")
    private String inputText;

    @Column(name = "output_text")
    private String outputText;

    @Column(name = "translation_status")
    @Enumerated(EnumType.STRING)
    private Status status;
}
