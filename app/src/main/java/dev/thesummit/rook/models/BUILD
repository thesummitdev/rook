load("@rules_java//java:defs.bzl", "java_library")

package(default_visibility = ["//visibility:public"])

java_library(
    name = "models",
    srcs = [
        "BaseModel.java",
        "Link.java",
        "Preference.java",
        "User.java",
        "ApiKey.java",
        "SystemKey.java",
    ],
    deps = [
        "//app/src/main/java/dev/thesummit/rook/database:database_field",
        "@maven//:commons_validator_commons_validator",
        "@maven//:org_json_json",
    ],
)
