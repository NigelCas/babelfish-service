package com.trabeya.engineering.babelfish.service.controllers;

import com.google.cloud.translate.Detection;
import com.google.cloud.translate.Language;
import com.trabeya.engineering.babelfish.service.client.GoogleTranslateClient;
import com.trabeya.engineering.babelfish.service.controllers.assemblers.TranslationResourceAssembler;
import com.trabeya.engineering.babelfish.service.exceptions.TranslationNotFoundException;
import com.trabeya.engineering.babelfish.service.model.TranslationModel;
import com.trabeya.engineering.babelfish.service.model.TranslationStatus;
import com.trabeya.engineering.babelfish.service.repository.TranslationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;


@RequestMapping("/babelfish/translator")
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
    ResponseEntity<String> test() {
        // The test text to translate
        String text = "This is a test!";
        String translation = googleTranslateClient.translateTextV2(
                text, "text","en", "es");
        log.info(String.format("Test Text: %s%n", text));
        log.info(String.format("Test Translation: %s%n", translation));
        return ResponseEntity.ok(text + " || " + translation);
    }


    @GetMapping("/translations/{id}")
    public Resource<TranslationModel> one(@PathVariable Long id) {

        TranslationModel translation = translationRepository.findById(id)
                .orElseThrow(() -> new TranslationNotFoundException(id));

        return new Resource<>(translation,
                linkTo(methodOn(TranslationController.class).one(id)).withSelfRel(),
                linkTo(methodOn(TranslationController.class).all()).withRel("translations"));
    }


    @GetMapping("/translations")
    public Resources<Resource<TranslationModel>> all() {

        List<Resource<TranslationModel>> translations = translationRepository.findAll().stream()
                .map(employee -> new Resource<>(employee,
                        linkTo(methodOn(TranslationController.class).one(employee.getId())).withSelfRel(),
                        linkTo(methodOn(TranslationController.class).all()).withRel("translations")))
                .collect(Collectors.toList());

        return new Resources<>(translations,
                linkTo(methodOn(TranslationController.class).all()).withSelfRel());
    }

    @PostMapping("/translation")
    public ResponseEntity<?> newTranslation(@RequestBody TranslationModel newTranslation) throws URISyntaxException {

        ResponseEntity<?> response = null;

        try {
            // commit translation to database
            newTranslation.setStatus(TranslationStatus.IN_PROGRESS);
            TranslationModel newOrder = translationRepository.save(newTranslation);

            String inputLanguageCode = null;
            String outputLanguageCode = null;

            // If request misses input language code detect request text language with GCloud ClientLib
            List<Language> languages = googleTranslateClient.getListSupportedLanguagesV2();
            if (newTranslation.getInputLanguage().trim().isEmpty()) {
                log.info("Input language not given, recognizing text....");
                Detection detection = googleTranslateClient.detectTextLangV2(newTranslation.getInputText());
                for (Language lang : languages) {
                    if (lang.getCode().equals(detection.getLanguage())) {
                        inputLanguageCode = lang.getCode();
                        newOrder.setInputLanguage(lang.getName());
                        break;
                    }
                }
                log.info("Recognized text as :" + newTranslation.getInputLanguage() + "(" + inputLanguageCode + ")");
            } else {
                for (Language lang : languages) {
                    if (lang.getName().equalsIgnoreCase(newTranslation.getInputLanguage())) {
                        inputLanguageCode = lang.getCode();
                        newOrder.setInputLanguage(lang.getName());
                    }
                    if (lang.getName().equalsIgnoreCase(newTranslation.getOutputLanguage())) {
                        outputLanguageCode = lang.getCode();
                        newOrder.setInputLanguage(lang.getName());
                    }
                }
            }

            if ((null != inputLanguageCode) && (null != outputLanguageCode)) {
                boolean isTranslationSupported = false;
                for (Language lang : googleTranslateClient.listSupportedLanguagesV2(outputLanguageCode)) {
                    if (lang.getCode().equals(inputLanguageCode)) {
                        isTranslationSupported = true;
                        newOrder.setInputText(newTranslation.getInputText());
                        break;
                    }
                }
                if ((isTranslationSupported)
                        && (null != newTranslation.getOutputFormat())
                        && !(newTranslation.getOutputFormat().isEmpty())) {
                    newOrder.setOutputFormat(newTranslation.getOutputFormat());
                    if (newTranslation.getOutputFormat().equalsIgnoreCase("text")) {
                        newOrder.setOutputText(googleTranslateClient.translateTextV2(
                                newTranslation.getInputText(),
                                "text", inputLanguageCode, outputLanguageCode));
                    }
                    else if (newTranslation.getOutputFormat().equalsIgnoreCase("html")) {
                        newOrder.setOutputText(googleTranslateClient.translateTextV2(
                                newTranslation.getInputText(),
                                "HTML", inputLanguageCode, outputLanguageCode));
                    }

                }
            }

            // return state of translation is committed to DB
            Resource<TranslationModel> resource
                    = translationResourceAssembler.toResource(translationRepository.save(newOrder));
            response = ResponseEntity
                    .created(new URI(resource.getId().expand().getHref()))
                    .body(resource);


        } catch (Exception ex) {

        }

        return response;
    }


}
