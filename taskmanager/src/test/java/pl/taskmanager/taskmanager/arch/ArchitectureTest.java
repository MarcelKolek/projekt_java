package pl.taskmanager.taskmanager.arch;

@com.tngtech.archunit.junit.AnalyzeClasses(packages = "pl.taskmanager.taskmanager")
class ArchitectureTest {

    @com.tngtech.archunit.junit.ArchTest
    static final com.tngtech.archunit.lang.ArchRule layers_should_be_respected = com.tngtech.archunit.library.Architectures.layeredArchitecture()
            .consideringAllDependencies()
            .layer("Controller").definedBy("..controller..")
            .layer("Service").definedBy("..service..")
            .layer("Repository").definedBy("..repository..")
            .layer("Entity").definedBy("..entity..")
            .layer("Config").definedBy("..config..")

            .whereLayer("Controller").mayNotBeAccessedByAnyLayer()
            .whereLayer("Service").mayOnlyBeAccessedByLayers("Controller", "Config")
            .whereLayer("Repository").mayOnlyBeAccessedByLayers("Service", "Controller");

    @com.tngtech.archunit.junit.ArchTest
    static final com.tngtech.archunit.lang.ArchRule controllers_should_not_depend_on_entities = com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes()
        .that().resideInAPackage("..controller..")
        .should().onlyDependOnClassesThat().resideInAnyPackage(
            "..dto..", "..service..", "..dao..", "..exception..", "..controller..", "..config..", "..entity..",
            "java..", "org.springframework..", "io.swagger..", "jakarta.validation..", "org.slf4j..", "com.fasterxml.jackson..",
            "org.junit..", "org.mockito..", "com.lowagie.text..", "org.hamcrest..", "jakarta.servlet..", "org.apache.tomcat.."
        );

    @com.tngtech.archunit.junit.ArchTest
    static final com.tngtech.archunit.lang.ArchRule services_should_be_in_service_package = com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes()
            .that().haveSimpleNameEndingWith("Service")
            .should().resideInAPackage("..service..");
}
