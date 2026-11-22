package de.anashabib.lyricsync;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;

import static org.assertj.core.api.Assertions.assertThat;

class ModularityTest {
    private static final ApplicationModules modules = ApplicationModules.of(LyricsyncApplication.class);

    @Test
    void detectsTheCatalogModule() {
        // without this, verify() would pass on zero modules if the package layout ever broke
        assertThat(modules.getModuleByName("catalog")).isPresent();
    }

    @Test
    void modulesOnlyUseEachOthersPublicApi() {
        modules.verify();
    }

    @Test
    void writesModuleDocumentation() {
        // diagrams and module canvases end up in target/spring-modulith-docs
        new Documenter(modules).writeDocumentation();
    }
}
