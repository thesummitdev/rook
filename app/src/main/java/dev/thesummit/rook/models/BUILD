load("@rules_java//java:defs.bzl", "java_library")

package(default_visibility = ["//visibility:public"])

java_library(
    name = "models",
    srcs = [
        "BaseModel.java",
        "ApiKey.java",
        "Link.java",
        "PageableBaseModel.java",
        "Preference.java",
        "SystemKey.java",
        "User.java",
    ],
    deps = [
        "//app/src/main/java/dev/thesummit/rook/database:database_field",
        "@maven//:commons_validator_commons_validator",
        "@maven//:org_json_json",
    ],
)
