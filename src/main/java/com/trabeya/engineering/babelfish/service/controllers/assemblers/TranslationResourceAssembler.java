package com.trabeya.engineering.babelfish.service.controllers.assemblers;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.*;

import com.trabeya.engineering.babelfish.service.controllers.TranslationController;
import com.trabeya.engineering.babelfish.service.model.TranslationRequest;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceAssembler;
import org.springframework.stereotype.Component;

@Component
public
class TranslationResourceAssembler implements ResourceAssembler<TranslationRequest, Resource<TranslationRequest>> {

    @Override
    public Resource<TranslationRequest> toResource(TranslationRequest employee) {

        return new Resource<>(employee,
                linkTo(methodOn(TranslationController.class).one(employee.getId())).withSelfRel(),
                linkTo(methodOn(TranslationController.class).all()).withRel("employees"));
    }
}
