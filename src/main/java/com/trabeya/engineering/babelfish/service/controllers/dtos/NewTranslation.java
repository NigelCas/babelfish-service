package com.trabeya.engineering.babelfish.service.controllers.dtos;

import com.trabeya.engineering.babelfish.service.model.TranslationOutputFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NewTranslation {

    private String inputLanguage;
    @NotBlank(message = "Must not be blank")
    private String outputLanguage;
    private TranslationOutputFormat outputFormat;
    @NotBlank(message = "Must not be blank")
    private String inputText;


}
