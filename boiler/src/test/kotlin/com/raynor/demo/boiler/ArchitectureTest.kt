package com.raynor.demo.boiler

import com.tngtech.archunit.library.Architectures
import io.kotest.core.spec.style.FunSpec

class ArchitectureTest :
    FunSpec(
        {
            test("controller layer should not depend on infra layer") {
                Architectures.layeredArchitecture()
                    .consideringAllDependencies()
                    .layer("controller").definedBy("com.raynor.demo.boiler.controller..")
                    .layer("service").definedBy("com.raynor.demo.boiler.service..")
                    .layer("domain").definedBy("com.raynor.demo.boiler.domain..")
                    .layer("infra").definedBy("com.raynor.demo.boiler.infra..")
                    .whereLayer("controller").mayNotBeAccessedByAnyLayer()
                    .whereLayer("service").mayOnlyBeAccessedByLayers("controller")
                    .whereLayer("domain").mayOnlyBeAccessedByLayers("service")
                    .whereLayer("infra").mayOnlyBeAccessedByLayers("service")
            }
        },
    )
