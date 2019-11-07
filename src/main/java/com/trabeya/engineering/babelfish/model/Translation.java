package com.trabeya.engineering.babelfish.model;

import lombok.Data;
import org.hibernate.annotations.Type;
import javax.persistence.*;

@Data
@Entity
@Table(name = "translation")
public class Translation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private  Long id;

    @Column(name = "input_language")
    private String inputLanguage;

    @Column(name = "output_language")
    private String outputLanguage;

    @Column(name = "translation_format")
    @Enumerated(EnumType.STRING)
    private TranslationOutputFormat outputFormat;

    @Lob
    @Type(type="text")
    @Column(name = "input_text", columnDefinition="CLOB")
    private String inputText;

    @Lob
    @Type(type="text")
    @Column(name = "output_text", columnDefinition="CLOB")
    private String outputText;

    @Column(name = "translation_status")
    @Enumerated(EnumType.STRING)
    private Status status;

    @Version
    @Column(name = "version")
    private int version;
}
