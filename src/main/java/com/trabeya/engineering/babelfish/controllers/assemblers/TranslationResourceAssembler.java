package com.trabeya.engineering.babelfish.controllers.assemblers;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import com.trabeya.engineering.babelfish.controllers.TranslationController;
import com.trabeya.engineering.babelfish.model.Translation;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

@Component
public class TranslationResourceAssembler implements RepresentationModelAssembler<Translation, EntityModel<Translation>> {

    @Override
    public EntityModel<Translation> toModel(Translation translation) {
        return new EntityModel<>(translation,
                linkTo(methodOn(TranslationController.class).getTranslation(translation.getId())).withSelfRel(),
                linkTo(methodOn(TranslationController.class).getAllTranslations()).withRel("translations"));
    }
}
