package com.trabeya.engineering.babelfish.controllers.dtos;

import com.trabeya.engineering.babelfish.model.TranslationOutputFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NewTranslationDto {

    private String inputLanguage;
    @NotBlank(message = "Must not be blank")
    private String outputLanguage;
    @NotNull(message = "An Output Format must be specified")
    private TranslationOutputFormat outputFormat;
    @NotBlank(message = "Must not be blank")
    private String inputText;


}
