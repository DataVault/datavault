package org.datavaultplatform.webapp.controllers;

import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(packages = "org.datavaultplatform.webapp.controllers")
public class ApiConsistencyArchUnitTest {

    // New Rule 1: Controllers should reside in specific packages
    @ArchTest
    static final ArchRule controllers_should_reside_in_controllers_package =
            classes()
                    .that().areAnnotatedWith(Controller.class)
                    .or().areAnnotatedWith(RestController.class)
                    .should().resideInAPackage("..controllers..")
                    .as("Controllers should reside in a package named 'controller'");

    // New Rule 2: @RestController classes should not use @ResponseBody
    @ArchTest
    static final ArchRule rest_controllers_should_not_use_response_body =
            methods()
                    .that().areDeclaredInClassesThat().areAnnotatedWith(RestController.class)
                    .should().notBeAnnotatedWith(ResponseBody.class)
                    .as("Methods in @RestController classes should not use @ResponseBody");

    // New Rule 3: @GetMapping methods should not have @RequestBody parameters
    @ArchTest
    static final ArchRule get_mapping_methods_should_not_have_request_body =
            methods()
                    .that().areAnnotatedWith(GetMapping.class)
                    .should(new ArchCondition<>("not have @RequestBody parameter") {
                        @Override
                        public void check(JavaMethod method, ConditionEvents events) {
                            boolean hasRequestBodyParameter = method.getParameters().stream()
                                    .anyMatch(param -> param.isAnnotatedWith(RequestBody.class));
                            if (hasRequestBodyParameter) {
                                String message = String.format("Method %s is a @GetMapping but has a @RequestBody parameter", method.getFullName());
                                events.add(SimpleConditionEvent.violated(method, message));
                            }
                        }
                    })
                    .as("@GetMapping methods should not have @RequestBody parameters");

    // New Rule 4: @DeleteMapping methods should not have @RequestBody parameters
    @ArchTest
    static final ArchRule delete_mapping_methods_should_not_have_request_body =
            methods()
                    .that().areAnnotatedWith(DeleteMapping.class)
                    .should(new ArchCondition<>("not have @RequestBody parameter") {
                        @Override
                        public void check(JavaMethod method, ConditionEvents events) {
                            boolean hasRequestBodyParameter = method.getParameters().stream()
                                    .anyMatch(param -> param.isAnnotatedWith(RequestBody.class));
                            if (hasRequestBodyParameter) {
                                String message = String.format("Method %s is a @DeleteMapping but has a @RequestBody parameter", method.getFullName());
                                events.add(SimpleConditionEvent.violated(method, message));
                            }
                        }
                    })
                    .as("@DeleteMapping methods should not have @RequestBody parameters");

    // New Rule 5: All @RequestMapping methods should be public
    @ArchTest
    static final ArchRule request_mapping_methods_should_be_public =
            methods()
                    .that().areAnnotatedWith(RequestMapping.class)
                    .or().areAnnotatedWith(GetMapping.class)
                    .or().areAnnotatedWith(PostMapping.class)
                    .or().areAnnotatedWith(PutMapping.class)
                    .or().areAnnotatedWith(DeleteMapping.class)
                    .or().areAnnotatedWith(PatchMapping.class)
                    .should().bePublic()
                    .as("All @RequestMapping (and derivatives) methods should be public");

    // New Rule 6: Controller methods should not directly access repositories
    @ArchTest
    static final ArchRule controller_methods_should_not_access_repositories =
            noClasses()
                    .that().areAnnotatedWith(Controller.class)
                    .or().areAnnotatedWith(RestController.class)
                    .should().accessClassesThat().resideInAPackage("..repository..") // Corrected rule structure
                    .as("Controllers should not directly access classes in repository packages");
}
