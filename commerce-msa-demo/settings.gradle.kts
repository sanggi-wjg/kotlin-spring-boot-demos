rootProject.name = "commerce-msa-demo"

include("shared")
project(":shared").name = "shared"
project(":shared").projectDir = file("shared")

include("services:product-service")
project(":services:product-service").name = "product-service"
project(":services:product-service").projectDir = file("services/product-service")
