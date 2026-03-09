package org.datavaultplatform.webapp.controllers;

import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Arrays;
import java.util.List;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;

@AnalyzeClasses(packages = "org.datavaultplatform.webapp.controllers")
public class FormApiConsistencyTest {

    @ArchTest
    static final ArchRule post_methods_with_model_attribute_should_consume_form_data =
            methods()
                    .that().areAnnotatedWith(PostMapping.class)
                    .should(new ArchCondition<>("consume form-urlencoded or multipart form data") {
                        @Override
                        public void check(JavaMethod method, ConditionEvents events) {
                            // Check if any parameter has @ModelAttribute
                            boolean hasModelAttributeParameter = method.getParameters().stream()
                                    .anyMatch(param -> param.isAnnotatedWith(ModelAttribute.class));

                            if (hasModelAttributeParameter) {
                                PostMapping ann = method.getAnnotationOfType(PostMapping.class);
                                List<String> consumes = Arrays.asList(ann.consumes());

                                // Check if 'consumes' contains either form-urlencoded or multipart media type
                                boolean hasFormConsumes = consumes.contains(MediaType.APPLICATION_FORM_URLENCODED_VALUE) ||
                                        consumes.contains(MediaType.MULTIPART_FORM_DATA_VALUE);

                                if (!hasFormConsumes) {
                                    String message = String.format("Method %s has @ModelAttribute but is missing 'consumes = APPLICATION_FORM_URLENCODED_VALUE' or 'consumes = MULTIPART_FORM_DATA_VALUE'", method.getFullName());
                                    events.add(SimpleConditionEvent.violated(method, message));
                                }
                            }
                        }
                    });

    @ArchTest
    static final ArchRule post_methods_with_request_body_attribute_should_consume_json =
            methods()
                    .that().areAnnotatedWith(PostMapping.class)
                    .should(new ArchCondition<>("consume JSON data") {
                        @Override
                        public void check(JavaMethod method, ConditionEvents events) {
                            // Check if any parameter has @RequestBody
                            boolean hasRequestBodyParameter = method.getParameters().stream()
                                    .anyMatch(param -> param.isAnnotatedWith(RequestBody.class));

                            if (hasRequestBodyParameter) {
                                PostMapping ann = method.getAnnotationOfType(PostMapping.class);
                                List<String> consumes = Arrays.asList(ann.consumes());

                                // Check if 'consumes' contains APPLICATION_JSON_VALUE
                                boolean hasJsonConsumes = consumes.contains(MediaType.APPLICATION_JSON_VALUE);

                                if (!hasJsonConsumes) {
                                    String message = String.format("Method %s has @RequestBody but is missing 'consumes = APPLICATION_JSON_VALUE'", method.getFullName());
                                    events.add(SimpleConditionEvent.violated(method, message));
                                }
                            }
                        }
                    });
}
