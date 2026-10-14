package br.caio.delivery

import com.tngtech.archunit.core.importer.ImportOption
import com.tngtech.archunit.junit.AnalyzeClasses
import com.tngtech.archunit.junit.ArchTest
import com.tngtech.archunit.lang.ArchRule
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses

// O domínio vem do módulo :shared, que está no classpath do :api, então as regras cobrem os dois.
@AnalyzeClasses(
    packages = ["br.caio.delivery"],
    importOptions = [ImportOption.DoNotIncludeTests::class],
)
class ArquiteturaTest {
    @ArchTest
    val nucleoNaoDependeDeFramework: ArchRule =
        noClasses()
            .that()
            .resideInAnyPackage("..aplicacao..", "..dominio..")
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage(
                "io.ktor..",
                "org.jetbrains.exposed..",
                "java.sql..",
                "org.postgresql..",
            )

    @ArchTest
    val nucleoNaoDependeDaInfra: ArchRule =
        noClasses()
            .that()
            .resideInAnyPackage("..aplicacao..", "..dominio..")
            .should()
            .dependOnClassesThat()
            .resideInAPackage("..infra..")
}
