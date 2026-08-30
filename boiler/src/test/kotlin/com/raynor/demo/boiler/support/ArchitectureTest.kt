package com.raynor.demo.boiler.support

import com.tngtech.archunit.core.importer.ClassFileImporter
import com.tngtech.archunit.core.importer.ImportOption
import com.tngtech.archunit.library.Architectures
import io.kotest.core.spec.style.FunSpec

class ArchitectureTest :
    FunSpec(
        {

            val classes by lazy {
                ClassFileImporter()
                    .withImportOption(ImportOption.DoNotIncludeTests())
                    .importPackages("com.raynor.demo.boiler")
            }

            test("layer dependency") {
                Architectures.layeredArchitecture()
                    .consideringAllDependencies()
                    .layer("controller").definedBy("com.raynor.demo.boiler.controller..")
                    .layer("service").definedBy("com.raynor.demo.boiler.service..")
                    .layer("repository").definedBy("com.raynor.demo.boiler.repository..")
                    .layer("domain").definedBy("com.raynor.demo.boiler.domain..")
                    .layer("infra").definedBy("com.raynor.demo.boiler.infra..")
                    .whereLayer("controller").mayNotBeAccessedByAnyLayer()
                    .whereLayer("service").mayOnlyBeAccessedByLayers("controller")
                    .whereLayer("repository").mayOnlyBeAccessedByLayers("service")
                    .whereLayer("domain").mayOnlyBeAccessedByLayers("service", "repository")
                    .whereLayer("infra").mayOnlyBeAccessedByLayers("service")
                    .check(classes)
            }
        },
    )
