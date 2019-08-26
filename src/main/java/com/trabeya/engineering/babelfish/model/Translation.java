package com.trabeya.engineering.babelfish.model;

import lombok.Data;
import javax.persistence.*;

@Data
@Entity
@Table(name = "translation")
public class Translation {

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

    @Lob
    @Column(name = "input_text", length=512)
    private String inputText;

    @Lob
    @Column(name = "output_text", length=512)
    private String outputText;

    @Column(name = "translation_status")
    @Enumerated(EnumType.STRING)
    private Status status;

    @Version
    @Column(name = "version")
    private int version;
}
