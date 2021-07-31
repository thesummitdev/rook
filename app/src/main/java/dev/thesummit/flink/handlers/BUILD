load("@rules_java//java:defs.bzl", "java_binary")

package(default_visibility = ["//visibility:public"])

java_library(
    name = "handlers",
    srcs = [
        "LinkHandler.java",
    ],
    deps = [
        "//app/src/main/java/dev/thesummit/flink/models",
        "@maven//:com_fasterxml_jackson_core_jackson_core",
        "@maven//:io_javalin_javalin_bundle",
        "@maven//:org_json_json",
    ],
)
