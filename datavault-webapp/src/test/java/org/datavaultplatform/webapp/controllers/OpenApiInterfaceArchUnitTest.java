package org.datavaultplatform.webapp.controllers;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import io.swagger.v3.oas.annotations.Operation;

import java.util.Optional;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

@AnalyzeClasses(packages = "org.datavaultplatform.webapp")
public class OpenApiInterfaceArchUnitTest {

    private static final DescribedPredicate<JavaClass> NOT_A_TEST_CLASS =
            new DescribedPredicate<>("is not a test class or inner class of a test class") {
                @Override
                public boolean test(JavaClass javaClass) {
                    String simpleName = javaClass.getSimpleName();

                    // 1. Check if the class itself is a test
                    if (simpleName.endsWith("Test") || simpleName.endsWith("IT")) {
                        return false;
                    }

                    // 2. Check if it's an inner class of a test (Fixing the Optional issue)
                    Optional<JavaClass> outerClass = javaClass.getEnclosingClass();
                    if (outerClass.isPresent()) {
                        String outerSimpleName = outerClass.get().getSimpleName();
                        return !outerSimpleName.endsWith("Test") && !outerSimpleName.endsWith("IT");
                    }
                    return true;
                }
            };

    @ArchTest
    static final ArchRule controllers_should_implement_api_interface_and_not_have_internal_docs =
            classes()
                    .that().resideInAPackage("org.datavaultplatform.webapp.controllers..")
                    .and().resideOutsideOfPackage("org.datavaultplatform.webapp.controllers.standalone..")
                    .and(NOT_A_TEST_CLASS)
                    .and().doNotHaveFullyQualifiedName("org.datavaultplatform.webapp.controllers.auth.ErrorController") // Explicitly ignore ErrorController
                    .and().areMetaAnnotatedWith(org.springframework.stereotype.Controller.class) // This covers both @Controller and @RestController
                    .should(new ArchCondition<>("implement their corresponding Api interface and have no local @Operation annotations") {
                        @Override
                        public void check(JavaClass controllerClass, ConditionEvents events) {
                            String expectedInterfaceName = controllerClass.getSimpleName() + "Api";

                            // 1. Check for Interface Implementation using getRawInterfaces()
                            boolean implementsApi = controllerClass.getRawInterfaces().stream()
                                    .anyMatch(i -> i.getSimpleName().equals(expectedInterfaceName));

                            if (!implementsApi) {
                                events.add(SimpleConditionEvent.violated(controllerClass,
                                        controllerClass.getName() + " does not implement " + expectedInterfaceName));
                            }

                            // 2. Check for @Operation annotations on methods
                            boolean hasLocalOperationDocs = controllerClass.getMethods().stream()
                                    .anyMatch(method -> method.isAnnotatedWith(Operation.class));

                            if (hasLocalOperationDocs) {
                                events.add(SimpleConditionEvent.violated(controllerClass,
                                        controllerClass.getName() + " contains @Operation annotations. These should be moved to the Api interface."));
                            }
                        }
                    })
                    .as("Controllers should implement their corresponding Api interface and have no local @Operation annotations");
}
