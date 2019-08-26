package com.trabeya.engineering.babelfish.controllers.assemblers;

import com.trabeya.engineering.babelfish.controllers.SpeechToTextController;
import com.trabeya.engineering.babelfish.model.SpeechToTextTranscription;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceAssembler;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

public class SpeechToTextSynthesisResourceAssembler
        implements ResourceAssembler<SpeechToTextTranscription, Resource<SpeechToTextTranscription>> {

    @Override
    public Resource<SpeechToTextTranscription> toResource(SpeechToTextTranscription entity) {
        return new Resource<>(entity,
            linkTo(methodOn(SpeechToTextController.class)
                    .getTextToSpeechTranscription(entity.getId())).withSelfRel(),
            linkTo(methodOn(SpeechToTextController.class)
                    .getAllTextToSpeechTranscriptions()).withRel("transcriptions"));
    }
}
