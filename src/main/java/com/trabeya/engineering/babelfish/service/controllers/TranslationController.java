package com.trabeya.engineering.babelfish.service.controllers;

import com.google.cloud.translate.Translation;
import com.trabeya.engineering.babelfish.service.controllers.assemblers.TranslationResourceAssembler;
import com.trabeya.engineering.babelfish.service.exceptions.TranslationNotFoundException;
import com.trabeya.engineering.babelfish.service.model.TranslationRequest;
import com.trabeya.engineering.babelfish.service.repository.TranslationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.google.cloud.translate.Translate;
import com.google.cloud.translate.Translate.TranslateOption;
import com.google.cloud.translate.TranslateOptions;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;


@RequestMapping("/babelfish/translator")
@RestController
public class TranslationController {

    @Autowired
    private TranslationRepository translationRepository;

    @Autowired
    private TranslationResourceAssembler translationResourceAssembler;

    @GetMapping("/test")
    ResponseEntity<String> test() {

        // Instantiates a client
        Translate translate = TranslateOptions.getDefaultInstance().getService();

        // The text to translate
        String text = "Hello, world!";

        // Translates some text into Russian
        Translation translation =
                translate.translate(
                        text,
                        TranslateOption.sourceLanguage("en"),
                        TranslateOption.targetLanguage("ru"));

        String inputText = String.format("Text: %s%n", text);
        String translatedText = String.format("Translation: %s%n", translation.getTranslatedText());
        return ResponseEntity.ok(inputText + " || " + translatedText);
    }


    @GetMapping("/translations/{id}")
    public Resource<TranslationRequest> one(@PathVariable Long id) {

        TranslationRequest employee = translationRepository.findById(id)
                .orElseThrow(() -> new TranslationNotFoundException(id));

        return new Resource<>(employee,
                linkTo(methodOn(TranslationController.class).one(id)).withSelfRel(),
                linkTo(methodOn(TranslationController.class).all()).withRel("translations"));
    }


    @GetMapping("/translations")
    public Resources<Resource<TranslationRequest>> all() {

        List<Resource<TranslationRequest>> employees = translationRepository.findAll().stream()
                .map(employee -> new Resource<>(employee,
                        linkTo(methodOn(TranslationController.class).one(employee.getId())).withSelfRel(),
                        linkTo(methodOn(TranslationController.class).all()).withRel("translations")))
                .collect(Collectors.toList());

        return new Resources<>(employees,
                linkTo(methodOn(TranslationController.class).all()).withSelfRel());
    }

    @PostMapping("/translation")
    ResponseEntity<?> newTranslation(@RequestBody TranslationRequest newEmployee) throws URISyntaxException {
        // TODO Validate request data & send to Google cloud translate
        Resource<TranslationRequest>
                resource = translationResourceAssembler.toResource(translationRepository.save(newEmployee));

        return ResponseEntity
                .created(new URI(resource.getId().expand().getHref()))
                .body(resource);
    }




}
