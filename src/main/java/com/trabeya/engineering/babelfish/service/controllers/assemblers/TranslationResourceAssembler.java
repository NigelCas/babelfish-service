package com.trabeya.engineering.babelfish.service.controllers.assemblers;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.*;

import com.trabeya.engineering.babelfish.service.controllers.TranslationController;
import com.trabeya.engineering.babelfish.service.model.TranslationModel;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceAssembler;
import org.springframework.stereotype.Component;

@Component
public
class TranslationResourceAssembler implements ResourceAssembler<TranslationModel, Resource<TranslationModel>> {

    @Override
    public Resource<TranslationModel> toResource(TranslationModel translation) {

        return new Resource<>(translation,
                linkTo(methodOn(TranslationController.class).getTranslation(translation.getId())).withSelfRel(),
                linkTo(methodOn(TranslationController.class).getAllTranslations()).withRel("translations"));
    }
}
