package com.trabeya.engineering.babelfish.controllers;

import com.google.cloud.translate.Detection;
import com.google.cloud.translate.Language;
import com.trabeya.engineering.babelfish.client.gcp.GoogleTranslateClient;
import com.trabeya.engineering.babelfish.controllers.assemblers.TranslationResourceAssembler;
import com.trabeya.engineering.babelfish.controllers.dtos.NewTranslationRequest;
import com.trabeya.engineering.babelfish.exceptions.*;
import com.trabeya.engineering.babelfish.model.Translation;
import com.trabeya.engineering.babelfish.model.Status;
import com.trabeya.engineering.babelfish.model.TranslationOutputFormat;
import com.trabeya.engineering.babelfish.repository.TranslationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.CollectionModel;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RequestMapping("/translator")
@RestController
@Slf4j
public class TranslationController {

    @Autowired
    private TranslationRepository translationRepository;

    @Autowired
    private TranslationResourceAssembler translationResourceAssembler;

    @Autowired
    private GoogleTranslateClient googleTranslateClient;

    @GetMapping("/test")
    public ResponseEntity<String> test() {
        // The test text to translate
        String text = "This is a test!";
        String translation = googleTranslateClient.translateTextV2(
                text, "text","en", "es");
        log.info(String.format("Test Text: %s%n", text));
        log.info(String.format("Test Translation: %s%n", translation));
        return ResponseEntity.ok(text + " || " + translation);
    }


    @GetMapping("/translations/{id}")
    public EntityModel<Translation> getTranslation(@PathVariable Long id) {

        Translation translation = translationRepository.findById(id)
                .orElseThrow(() -> new TranslationNotFoundException(id));

        return new EntityModel<>(translation,
                linkTo(methodOn(TranslationController.class).getTranslation(id)).withSelfRel(),
                linkTo(methodOn(TranslationController.class).getAllTranslations()).withRel("translations"));
    }


    @GetMapping("/translations")
    public CollectionModel<EntityModel<Translation>> getAllTranslations() {
        List<EntityModel<Translation>> translations = translationRepository.findAll().stream()
                .map(employee -> new EntityModel<>(employee,
                        linkTo(methodOn(TranslationController.class).getTranslation(employee.getId())).withSelfRel(),
                        linkTo(methodOn(TranslationController.class).getAllTranslations()).withRel("translations")))
                .collect(Collectors.toList());

        return new CollectionModel<>(translations,
                linkTo(methodOn(TranslationController.class).getAllTranslations()).withSelfRel());
    }

    @PostMapping("/translations")
    public ResponseEntity<EntityModel<Translation>> newTranslation(
            @RequestBody @Validated NewTranslationRequest translation) {

        ResponseEntity<EntityModel<Translation>> response = null;
        Translation inProgressTranslation = new Translation();
        inProgressTranslation.setStatus(Status.IN_PROGRESS);

        try {
            // commit translation to database
            BeanUtils.copyProperties(translation, inProgressTranslation);
            inProgressTranslation = translationRepository.save(inProgressTranslation);

            String inputLanguageCode;
            String outputLanguageCode = null;

            // If request misses input language code detect request text language with GCloud ClientLib
            if (!(StringUtils.hasText(translation.getInputLanguage()))) {
                log.info("Input language not given, recognizing text....");
                Detection detection = googleTranslateClient.detectTextLangV2(translation.getInputText());
                    inputLanguageCode = detection.getLanguage();
                    String detectedLanguage = getV2SupportedLanguageName4Code(detection.getLanguage());
                    translation.setInputLanguage(detectedLanguage);
                    inProgressTranslation.setInputLanguage(detectedLanguage);
                log.info("Recognized text as :" + detectedLanguage + "(" + inputLanguageCode + ")");
            }
            if (isV2LanguageNameValid(translation.getInputLanguage())
                    && isV2LanguageNameValid(translation.getOutputLanguage())) {
                    inputLanguageCode = getV2SupportedLanguageCode4Name(translation.getInputLanguage());
                    log.info("Input language code identified as : "+inputLanguageCode);
                    inProgressTranslation.setInputLanguage(translation.getInputLanguage());

                    outputLanguageCode = getV2SupportedLanguageCode4Name(translation.getOutputLanguage());
                    log.info("Output language code identified as : "+outputLanguageCode);
                    inProgressTranslation.setOutputLanguage(translation.getOutputLanguage());
                }
                else {
                    log.error("Error during language detection");
                    inProgressTranslation.setStatus(Status.FAILED);
                    throw new TranslationLanguageNotSupportedException(translation);
            }

            if (isV2LanguageTranslationSupported(inputLanguageCode, outputLanguageCode)) {
                inProgressTranslation.setInputText(translation.getInputText());
                if (translation.getOutputFormat().equals(TranslationOutputFormat.TEXT)) {
                    inProgressTranslation.setOutputText(googleTranslateClient.translateTextV2(
                            translation.getInputText(),
                            "text", inputLanguageCode, outputLanguageCode));
                    inProgressTranslation.setOutputFormat(TranslationOutputFormat.TEXT);
                    inProgressTranslation.setStatus(Status.COMPLETED);
                    log.info("Language Translation Completed");
                } else if (translation.getOutputFormat().equals(TranslationOutputFormat.HTML)) {
                    inProgressTranslation.setOutputText(googleTranslateClient.translateTextV2(
                            translation.getInputText(),
                            "html", inputLanguageCode, outputLanguageCode));
                    inProgressTranslation.setOutputFormat(TranslationOutputFormat.HTML);
                    inProgressTranslation.setStatus(Status.COMPLETED);
                    log.info("Language Translation Completed");
                } else {
                    log.error("Error during translation");
                    inProgressTranslation.setStatus(Status.FAILED);
                    throw new TranslationFormatNotSupportedException(translation);
                }
            }

        } catch (Exception ex) {
            log.error("POST /babelfish/translator/translations service error", ex);
            inProgressTranslation.setStatus(Status.FAILED);
            inProgressTranslation = translationRepository.save(inProgressTranslation);
            throw new TranslationFailedException(translation);
        }
        finally {
            // return state of translation is committed to DB
            EntityModel<Translation> resource
                    = translationResourceAssembler.toModel(translationRepository.save(inProgressTranslation));
            try {
                response = ResponseEntity
                        .created(new URI(resource.getRequiredLink("translations").expand().getHref()))
                        .body(resource);
            } catch (URISyntaxException e) {
                log.error("POST /babelfish/translator/translations service URI :", e);
            }
        }
        return response;
    }

    @GetMapping("/translations/support/languages")
    public CollectionModel<EntityModel<Language>> gcpSupportedLanguageList() {

        List<EntityModel<Language>> translations = new ArrayList<>();
        List<Language> languageList = googleTranslateClient.listSupportedLanguagesV2();
        for (Language language : languageList) {
            EntityModel<Language> languageResource = new EntityModel<>(language);
            translations.add(languageResource);
        }
        return new CollectionModel<>(translations);

    }

    @GetMapping("/translations/support/languages/{code}")
    public CollectionModel<EntityModel<Language>> gcpSupportedLanguageList(@PathVariable String code) {

        List<EntityModel<Language>> translations = new ArrayList<>();
        for (Language language : googleTranslateClient.listSupportedLanguagesV2(code)) {
            EntityModel<Language> languageResource = new EntityModel<>(language);
            translations.add(languageResource);
        }
        return new CollectionModel<>(translations);

    }


    private boolean isV2LanguageTranslationSupported(String inputCode, String outputCode) {
        boolean result = false;
        if (StringUtils.hasText(inputCode) && StringUtils.hasText(outputCode)) {
            List<Language> languages = googleTranslateClient.listSupportedLanguagesV2(outputCode);
            for (Language lang : languages) {
                if (lang.getCode().equals(inputCode)) {
                    result = true;
                    break;
                }
            }
        }
        return result;
    }


    private boolean isV2LanguageCodeValid(String code) {
        boolean result = false;
        if (StringUtils.hasText(code)) {
            List<Language> languages = googleTranslateClient.listSupportedLanguagesV2();
            for (Language lang : languages) {
                if (lang.getCode().equals(code)) {
                    result = true;
                    break;
                }
            }
        }
        return result;
    }


    private boolean isV2LanguageNameValid(String name) {
        boolean result = false;
        if (StringUtils.hasText(name)) {
            List<Language> languages = googleTranslateClient.listSupportedLanguagesV2();
            for (Language lang : languages) {
                if (lang.getName().equalsIgnoreCase(name)) {
                    result = true;
                    break;
                }
            }
        }
        return result;
    }

    private String getV2SupportedLanguageCode4Name(String name) {
        String result = null;
        List<Language> languages = googleTranslateClient.listSupportedLanguagesV2();
        for (Language lang : languages) {
            if (lang.getName().equalsIgnoreCase(name)) {
                result = lang.getCode();
                break;
            }
        }
        return result;
    }

    private String getV2SupportedLanguageName4Code(String code) {
        String result = "";
        List<Language> languages = googleTranslateClient.listSupportedLanguagesV2();
        for (Language lang : languages) {
            if (lang.getCode().equals(code)) {
                result = lang.getName();
                break;
            }
        }
        return result;
    }

}
