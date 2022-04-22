load("@rules_java//java:defs.bzl", "java_library")

package(default_visibility = ["//visibility:public"])

java_library(
    name = "auth",
    srcs = [
        "AuthModule.java",
        "RookPasswordManager.java",
        "JWTGenerator.java",
        "JWTProvider.java",
        "JWTResponse.java",
        "PasswordManager.java",
    ],
    deps = [
        "//app/src/main/java/dev/thesummit/rook/database:database_service",
        "//app/src/main/java/dev/thesummit/rook/models",
        "@maven//:com_auth0_java_jwt",
        "@maven//:com_google_inject_guice",
        "@maven//:org_slf4j_slf4j_api",
    ],
)
