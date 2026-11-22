package de.anashabib.lyricsync;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import jakarta.persistence.Entity;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.RestController;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * Layers inside every module: adapters -> application -> domain.
 * The boundaries between modules are checked by {@link ModularityTest}.
 */
class HexagonalArchitectureTest {
    private static final JavaClasses productionClasses = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("de.anashabib.lyricsync");

    @Test
    void domainDependsOnlyOnTheJdkAndItself() {
        classes().that().resideInAPackage("..domain..")
                .should().onlyDependOnClassesThat().resideInAnyPackage("java..", "..domain..")
                .because("the domain stays free of Spring, JPA, Lombok and adapters")
                .check(productionClasses);
    }

    @Test
    void applicationDoesNotKnowAdaptersOrConfig() {
        noClasses().that().resideInAPackage("..application..")
                .should().dependOnClassesThat().resideInAnyPackage("..adapter..", "..config..")
                .because("adapters plug into the application through ports, not the other way round")
                .check(productionClasses);
    }

    @Test
    void inboundAdaptersDoNotUseOutboundAdapters() {
        noClasses().that().resideInAPackage("..adapter.in..")
                .should().dependOnClassesThat().resideInAPackage("..adapter.out..")
                .because("a controller reaches the database only through a use case")
                .check(productionClasses);
    }

    @Test
    void outboundAdaptersDoNotUseInboundAdapters() {
        noClasses().that().resideInAPackage("..adapter.out..")
                .should().dependOnClassesThat().resideInAPackage("..adapter.in..")
                .check(productionClasses);
    }

    @Test
    void jpaEntitiesStayInThePersistenceAdapter() {
        classes().that().areAnnotatedWith(Entity.class)
                .should().resideInAPackage("..adapter.out.persistence..")
                .check(productionClasses);
    }

    @Test
    void controllersStayInTheWebAdapter() {
        classes().that().areAnnotatedWith(RestController.class)
                .should().resideInAPackage("..adapter.in.web..")
                .check(productionClasses);
    }
}
