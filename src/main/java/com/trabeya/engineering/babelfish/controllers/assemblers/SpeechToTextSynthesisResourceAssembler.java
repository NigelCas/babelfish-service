package com.trabeya.engineering.babelfish.controllers.assemblers;

import com.trabeya.engineering.babelfish.controllers.SpeechToTextController;
import com.trabeya.engineering.babelfish.model.SpeechToTextTranscription;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class SpeechToTextSynthesisResourceAssembler
        implements RepresentationModelAssembler<SpeechToTextTranscription, EntityModel<SpeechToTextTranscription>> {

    @Override
    public EntityModel<SpeechToTextTranscription> toModel(SpeechToTextTranscription speechToTextTranscription) {
        return new EntityModel<>(speechToTextTranscription,
            linkTo(methodOn(SpeechToTextController.class)
                    .getTextToSpeechTranscription(speechToTextTranscription.getId())).withSelfRel(),
            linkTo(methodOn(SpeechToTextController.class)
                    .getAllTextToSpeechTranscriptions()).withRel("transcriptions"));
    }
}
