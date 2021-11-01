load("@rules_java//java:defs.bzl", "java_binary")
load("@bazel_common//tools/maven:pom_file.bzl", "pom_file")
load("@npm//@angular-devkit/architect-cli:index.bzl", "architect", "architect_test")

package(default_visibility = ["//visibility:public"])

package_group(
    name = "flink_app",
    packages = [
        "//app/...",
    ],
)

java_binary(
    name = "flink",
    srcs = [
        "app/src/main/java/dev/thesummit/flink/FlinkApplication.java",
    ],
    main_class = "dev.thesummit.flink.FlinkApplication",
    resources =
        [
            ":web_bundle",
            "//app/src/main/resources",
        ],
    deps = [
        "//app/src/main/java/dev/thesummit/flink/auth",
        "//app/src/main/java/dev/thesummit/flink/database:database_module",
        "//app/src/main/java/dev/thesummit/flink/handlers",
        "//app/src/main/java/dev/thesummit/flink/models",
        "@maven//:com_fasterxml_jackson_core_jackson_core",
        "@maven//:com_google_inject_guice",
        "@maven//:commons_validator_commons_validator",
        "@maven//:io_javalin_javalin",
        "@maven//:org_slf4j_slf4j_api",
    ],
)

java_binary(
    name = "flink-dev",
    srcs = [
        "app/src/main/java/dev/thesummit/flink/FlinkApplication.java",
    ],
    main_class = "dev.thesummit.flink.FlinkApplication",
    resources =
        [
            ":web_bundle_dev",
            "//app/src/main/resources",
        ],
    deps = [
        "//app/src/main/java/dev/thesummit/flink/auth",
        "//app/src/main/java/dev/thesummit/flink/database:database_module",
        "//app/src/main/java/dev/thesummit/flink/handlers",
        "//app/src/main/java/dev/thesummit/flink/models",
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

filegroup(
    name = "common_deps",
    srcs = [
        "angular.json",
        "tsconfig.json",
    ],
    visibility = ["//:__subpackages__"],
)

APPLICATION_DEPS = [
    ":common_deps",
    "@npm//@angular/animations",
    "@npm//@angular/cdk",
    "@npm//@angular/cli",
    "@npm//@angular/core",
    "@npm//@angular/forms",
    "@npm//@angular/router",
    "@npm//@angular/platform-browser-dynamic",
    "@npm//@angular-devkit/build-angular",
    "@npm//rxjs",
    "@npm//mime",
    "@npm//zone.js",
    "@npm//tslib",
    "@npm//typescript",
]

architect(
    name = "build",
    args = [
        "frontend:build",
        "--outputPath=$(@D)",
    ],
    configuration_env_vars = ["NG_BUILD_CACHE"],
    data = glob(
        [
            "web/**/*",
        ],
        exclude = [
            "web/**/*.spec.ts",
            "web/src/test.ts",
        ],
    ) + APPLICATION_DEPS + [
        "tsconfig.app.json",
    ],
    output_dir = True,
)

architect(
    name = "build_prod",
    args = [
        "frontend:build:production",
        "--outputPath=$(@D)",
    ],
    configuration_env_vars = ["NG_BUILD_CACHE"],
    data = glob(
        [
            "web/src/**/*",
        ],
        exclude = [
            "web/src/**/*.spec.ts",
            "web/src/test.ts",
        ],
    ) + APPLICATION_DEPS + [
        "tsconfig.app.json",
    ],
    output_dir = True,
)

# Uncomment this for building the production binary without sourcemaps.
#genrule(
#name = "web_bundle",
#outs = [
#"web/index.html",
#"web/favicon.ico",
#"web/main-es2015.js",
#"web/main-es5.js",
#"web/polyfills-es2015.js",
#"web/polyfills-es5.js",
#"web/vendor-es2015.js",
#"web/vendor-es5.js",
#"web/runtime-es2015.js",
#"web/runtime-es5.js",
#"web/styles.css",
#],
#cmd = """cp $(locations :build_prod)/* $(RULEDIR)/web/.""",
#tools = [":build_prod"],
#)

genrule(
    name = "web_bundle_dev",
    outs = [
        "web/index.html",
        "web/favicon.ico",
        "web/main-es2015.js",
        "web/main-es2015.js.map",
        "web/main-es5.js",
        "web/main-es5.js.map",
        "web/polyfills-es2015.js",
        "web/polyfills-es2015.js.map",
        "web/polyfills-es5.js",
        "web/polyfills-es5.js.map",
        "web/vendor-es2015.js",
        "web/vendor-es2015.js.map",
        "web/vendor-es5.js",
        "web/vendor-es5.js.map",
        "web/runtime-es2015.js",
        "web/runtime-es2015.js.map",
        "web/runtime-es5.js",
        "web/runtime-es5.js.map",
        "web/styles.css",
        "web/styles.css.map",
    ],
    cmd = """cp $(locations :build)/* $(RULEDIR)/web/.""",
    tools = [":build"],
)
