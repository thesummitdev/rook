load("@rules_java//java:defs.bzl", "java_library")

package(default_visibility = ["//visibility:public"])

java_library(
    name = "flag_module",
    srcs = [
        "FlagModule.java",
    ],
    deps = [
        ":flag_service",
        "@maven//:com_google_inject_guice",
        "@maven//:org_slf4j_slf4j_api",
    ],
)

java_library(
    name = "flag_service",
    srcs = [
        "FlagService.java",
        "Flag.java",
    ],
    deps = [
        "@maven//:com_google_inject_guice",
        "@maven//:org_slf4j_slf4j_api",
    ],
)
