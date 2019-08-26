package com.trabeya.engineering.babelfish.controllers.assemblers;

import com.trabeya.engineering.babelfish.controllers.TextToSpeechController;
import com.trabeya.engineering.babelfish.model.TextToSpeechSynthesis;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceAssembler;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

public class TextToSpeechSynthesisResourceAssembler
        implements ResourceAssembler<TextToSpeechSynthesis, Resource<TextToSpeechSynthesis>> {

    @Override
    public Resource<TextToSpeechSynthesis> toResource(TextToSpeechSynthesis entity) {
        return new Resource<>(entity,
            linkTo(methodOn(TextToSpeechController.class)
                    .getTextToSpeechSynthesization(entity.getId())).withSelfRel(),
            linkTo(methodOn(TextToSpeechController.class)
                    .getAllTextToSpeechSynthesizations()).withRel("synthesizations"));
    }
}
