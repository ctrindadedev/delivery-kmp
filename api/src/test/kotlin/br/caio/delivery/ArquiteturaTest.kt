package br.caio.delivery

import br.caio.delivery.aplicacao.violacao.CasoDeUsoQueConheceAInfra
import br.caio.delivery.aplicacao.violacao.CasoDeUsoQueConheceOFramework
import com.tngtech.archunit.core.importer.ClassFileImporter
import com.tngtech.archunit.core.importer.ImportOption
import com.tngtech.archunit.junit.AnalyzeClasses
import com.tngtech.archunit.junit.ArchTest
import com.tngtech.archunit.lang.ArchRule
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

object RegrasDeArquitetura {
    private val nucleo = arrayOf("..aplicacao..", "..dominio..")

    val nucleoNaoDependeDeFramework: ArchRule =
        noClasses()
            .that()
            .resideInAnyPackage(*nucleo)
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage(
                "io.ktor..",
                "org.jetbrains.exposed..",
                "java.sql..",
                "org.postgresql..",
                "org.koin..",
            )

    val nucleoNaoDependeDeGrpc: ArchRule =
        noClasses()
            .that()
            .resideInAnyPackage(*nucleo)
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage(
                "io.grpc..",
                "com.google.protobuf..",
                "br.caio.delivery.contratos..",
            )

    val nucleoNaoDependeDaInfra: ArchRule =
        noClasses()
            .that()
            .resideInAnyPackage(*nucleo)
            .should()
            .dependOnClassesThat()
            .resideInAPackage("..infra..")
}

@AnalyzeClasses(
    packages = ["br.caio.delivery"],
    importOptions = [ImportOption.DoNotIncludeTests::class],
)
class ArquiteturaTest {
    @ArchTest
    val nucleoNaoDependeDeFramework = RegrasDeArquitetura.nucleoNaoDependeDeFramework

    @ArchTest
    val nucleoNaoDependeDeGrpc = RegrasDeArquitetura.nucleoNaoDependeDeGrpc

    @ArchTest
    val nucleoNaoDependeDaInfra = RegrasDeArquitetura.nucleoNaoDependeDaInfra
}

class RegrasDeArquiteturaDetectamViolacaoTest {
    @Test
    fun `acusa nucleo que depende de framework`() {
        val classes = ClassFileImporter().importClasses(CasoDeUsoQueConheceOFramework::class.java)

        assertThrows<AssertionError> { RegrasDeArquitetura.nucleoNaoDependeDeFramework.check(classes) }
    }

    @Test
    fun `acusa nucleo que depende da infra`() {
        val classes = ClassFileImporter().importClasses(CasoDeUsoQueConheceAInfra::class.java)

        assertThrows<AssertionError> { RegrasDeArquitetura.nucleoNaoDependeDaInfra.check(classes) }
    }
}
