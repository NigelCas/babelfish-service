package com.trabeya.engineering.babelfish.service.model;

import lombok.Data;
import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
@Entity
@Table(name = "translation")
public class TranslationModel {
    private @Id @GeneratedValue(strategy = GenerationType.AUTO) Long id;
    private String inputLanguage;
    @NotBlank(message = "Must not be blank")
    private String outputLanguage;
    private String outputFormat;
    @NotBlank(message = "Must not be blank")
    private String inputText;
    private String outputText;
    private TranslationStatus status;
}
