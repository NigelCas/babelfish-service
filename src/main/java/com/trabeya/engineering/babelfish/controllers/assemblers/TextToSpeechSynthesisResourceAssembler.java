package com.trabeya.engineering.babelfish.controllers.assemblers;

import com.trabeya.engineering.babelfish.controllers.TextToSpeechController;
import com.trabeya.engineering.babelfish.model.TextToSpeechSynthesis;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class TextToSpeechSynthesisResourceAssembler
        implements RepresentationModelAssembler<TextToSpeechSynthesis, EntityModel<TextToSpeechSynthesis>> {

    @Override
    public EntityModel<TextToSpeechSynthesis> toModel(TextToSpeechSynthesis textToSpeechSynthesis) {
        return new EntityModel<>(textToSpeechSynthesis,
            linkTo(methodOn(TextToSpeechController.class)
                    .getTextToSpeechSynthesization(textToSpeechSynthesis.getId())).withSelfRel(),
            linkTo(methodOn(TextToSpeechController.class)
                    .getAllTextToSpeechSynthesizations()).withRel("synthesizations"));
    }
}
