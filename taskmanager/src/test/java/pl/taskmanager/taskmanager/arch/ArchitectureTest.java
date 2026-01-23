package pl.taskmanager.taskmanager.arch;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

@AnalyzeClasses(packages = "pl.taskmanager.taskmanager")
class ArchitectureTest {

    @ArchTest
    static final ArchRule layers_should_be_respected = layeredArchitecture()
            .consideringAllDependencies()
            .layer("Controller").definedBy("..controller..")
            .layer("Service").definedBy("..service..")
            .layer("Repository").definedBy("..repository..")
            .layer("Entity").definedBy("..entity..")
            .layer("Config").definedBy("..config..")

            .whereLayer("Controller").mayNotBeAccessedByAnyLayer()
            .whereLayer("Service").mayOnlyBeAccessedByLayers("Controller", "Config")
            .whereLayer("Repository").mayOnlyBeAccessedByLayers("Service", "Controller"); // Controller allowed here for simplicity if needed

    @ArchTest
    static final ArchRule controllers_should_not_depend_on_entities = classes()
        .that().resideInAPackage("..controller..")
        .should().onlyDependOnClassesThat().resideInAnyPackage(
            "..dto..", "..service..", "..dao..", "..exception..", "..controller..", "..config..", "..entity..",
            "java..", "org.springframework..", "io.swagger..", "jakarta.validation..", "org.slf4j..", "com.fasterxml.jackson..",
            "org.junit..", "org.mockito..", "com.lowagie.text..", "org.hamcrest..", "jakarta.servlet..", "org.apache.tomcat.."
        );

    @ArchTest
    static final ArchRule services_should_be_in_service_package = classes()
            .that().haveSimpleNameEndingWith("Service")
            .should().resideInAPackage("..service..");
}
