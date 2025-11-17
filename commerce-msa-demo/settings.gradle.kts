rootProject.name = "commerce-msa-demo"

include("shared")
project(":shared").name = "shared"
project(":shared").projectDir = file("shared")

include("services:product-service")
project(":services:product-service").name = "product-service"
project(":services:product-service").projectDir = file("services/product-service")

include("services:order-service")
project(":services:order-service").name = "order-service"
project(":services:order-service").projectDir = file("services/order-service")
