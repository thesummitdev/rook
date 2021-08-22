load("@rules_java//java:defs.bzl", "java_binary")
load("@bazel_common//tools/maven:pom_file.bzl", "pom_file")

package(default_visibility = ["//visibility:public"])

package_group(
    name = "flink_app",
    packages = [
        "//app/...",
        "//web/...",
    ],
)

java_binary(
    name = "flink",
    srcs = ["app/src/main/java/dev/thesummit/flink/FlinkApplication.java"],
    main_class = "dev.thesummit.flink.FlinkApplication",
    resources = [
        "//app/src/main/resources",
        "//web:bundle",
        "//web:static_files",
    ],
    deps = [
        "//app/src/main/java/dev/thesummit/flink/auth",
        "//app/src/main/java/dev/thesummit/flink/database:database_module",
        "//app/src/main/java/dev/thesummit/flink/handlers",
        "//app/src/main/java/dev/thesummit/flink/models",
        "@maven//:com_auth0_java_jwt",
        "@maven//:com_fasterxml_jackson_core_jackson_core",
        "@maven//:com_google_inject_guice",
        "@maven//:commons_validator_commons_validator",
        "@maven//:io_javalin_javalin",
        "@maven//:org_slf4j_slf4j_api",
    ],
)

pom_file(
    name = "pom",
    testonly = True,
    targets = [
        ":flink",
        "//app/src/main/java/dev/thesummit/flink/database:database_module",
        "//app/src/main/java/dev/thesummit/flink/database:connection_pool",
        "//app/src/main/java/dev/thesummit/flink/database:database_service",
        "//app/src/main/java/dev/thesummit/flink/database:database_field",
        "//app/src/main/java/dev/thesummit/flink/auth",
        "//app/src/main/java/dev/thesummit/flink/models",
        "//app/src/main/java/dev/thesummit/flink/handlers",
        "//app/src/test/java/dev/thesummit/flink/handlers:LinkHandlerTest",
    ],
    template_file = "pom.template",
)
