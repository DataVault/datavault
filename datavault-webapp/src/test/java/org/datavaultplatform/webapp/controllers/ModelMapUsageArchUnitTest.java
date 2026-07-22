package org.datavaultplatform.webapp.controllers;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaMethodCall;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.springframework.ui.ModelMap;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(packages = "org.datavaultplatform.webapp")
public class ModelMapUsageArchUnitTest {

    /**
     * Custom predicate to identify calls to ModelMap.addAttribute(Object)
     */
    private static final DescribedPredicate<JavaMethodCall> CALLS_ADD_ATTRIBUTE_WITH_SINGLE_OBJECT =
            new DescribedPredicate<JavaMethodCall>("call ModelMap.addAttribute(Object)") {
                @Override
                public boolean test(JavaMethodCall call) {
                    // 1. Check if the class owning the called method is or extends ModelMap
                    boolean isModelMap = call.getTargetOwner().isAssignableTo(ModelMap.class);

                    // 2. Check if the method name is exactly "addAttribute"
                    boolean isAddAttributeMethod = call.getTarget().getName().equals("addAttribute");

                    // 3. Check if it has exactly ONE parameter of type java.lang.Object
                    boolean hasSingleObjectParam = call.getTarget().getRawParameterTypes().size() == 1 &&
                            call.getTarget().getRawParameterTypes().get(0).isEquivalentTo(Object.class);

                    return isModelMap && isAddAttributeMethod && hasSingleObjectParam;
                }
            };

    @ArchTest
    public static final ArchRule no_modelmap_add_attribute_with_single_object =
            noClasses()
                    .that().resideInAPackage("org.datavaultplatform.webapp..")
                    .should().callMethodWhere(CALLS_ADD_ATTRIBUTE_WITH_SINGLE_OBJECT)
                    .because("Relying on generated model attribute names can lead to brittle views. Always use addAttribute(String, Object).");

}