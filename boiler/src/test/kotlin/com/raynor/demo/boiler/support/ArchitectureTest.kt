package com.raynor.demo.boiler.support

import com.tngtech.archunit.base.DescribedPredicate
import com.tngtech.archunit.core.domain.JavaClass
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

            // enum 은 어느 레이어에서든 공유되는 값 타입으로 취급하므로 레이어 규칙 검사에서 제외한다.
            val isEnum = DescribedPredicate.describe<JavaClass>("enum") { it.isEnum }

            test("layer dependency") {
                Architectures.layeredArchitecture()
                    .consideringAllDependencies()
                    .ignoreDependency(DescribedPredicate.alwaysTrue(), isEnum)
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
