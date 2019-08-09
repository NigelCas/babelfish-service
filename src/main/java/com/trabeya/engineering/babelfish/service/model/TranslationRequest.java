package com.trabeya.engineering.babelfish.service.model;

import lombok.Data;
import javax.persistence.*;
import java.io.Serializable;

@Data
@Entity
@Table(name = "translation")
public class TranslationRequest {
    private @Id @GeneratedValue(strategy = GenerationType.AUTO) Long id;
    private String inputLanguage_code;
    private String outputLanguage_code;
    private String outputFormat;
    private String inputText;
}
