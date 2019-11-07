package com.trabeya.engineering.babelfish.controllers.assemblers;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.*;

import com.trabeya.engineering.babelfish.controllers.TranslationController;
import com.trabeya.engineering.babelfish.model.Translation;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceAssembler;
import org.springframework.stereotype.Component;

@Component
public class TranslationResourceAssembler implements ResourceAssembler<Translation, Resource<Translation>> {

    @Override
    public Resource<Translation> toResource(Translation translation) {
        return new Resource<>(translation,
                linkTo(methodOn(TranslationController.class).getTranslation(translation.getId())).withSelfRel(),
                linkTo(methodOn(TranslationController.class).getAllTranslations()).withRel("translations"));
    }
}
