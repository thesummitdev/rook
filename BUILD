load("@rules_java//java:defs.bzl", "java_binary")
load("@bazel_common//tools/maven:pom_file.bzl", "pom_file")
load("@npm//@angular-devkit/architect-cli:index.bzl", "architect")
load("@io_bazel_rules_docker//container:container.bzl", "container_image", "container_push")
load("@io_bazel_rules_docker//docker/package_managers:download_pkgs.bzl", "download_pkgs")
load("@io_bazel_rules_docker//docker/package_managers:install_pkgs.bzl", "install_pkgs")

package(default_visibility = ["//:rook_app"])

package_group(
    name = "rook_app",
    packages = [
        "//...",
        "//app/...",
        "//web/...",
    ],
)

# Builds the latest version of the container image.
# blaze run //:latest will add the image to the local docker instance.
container_image(
    name = "latest",
    base = "@debian_stable_linux_amd64//image",  # Our custom image base with the installed packages.
    creation_time = "{BUILD_TIMESTAMP}",
    entrypoint = [
        "/bin/bash",
        "-c",
        "./container_init.sh",
    ],
    env = {
        "ROOK_PORT": "8000",
        "ROOK_USER": "rook_system",
        "ROOK_PASSWORD": "rook_system",
        "DATA":"/usr/local/rook/data",
    },
    files = [
        "container_init.sh",
        ":rook_deploy.jar",
    ],
    repository = "thesummit/rook",
    stamp = "@io_bazel_rules_docker//stamp:always",
    tags = ["latest"],
)

container_image(
    name = "arm",
    base = "@debian_stable_linux_arm64_v8//image",  # Our custom image base with the installed packages.
    creation_time = "{BUILD_TIMESTAMP}",
    entrypoint = [
        "/bin/bash",
        "-c",
        "./container_init.sh",
    ],
    env = {
        "ROOK_PORT": "8000",
        "ROOK_USER": "rook_system",
        "ROOK_PASSWORD": "rook_system",
        "DATA":"/usr/local/rook/data",
    },
    files = [
        "container_init.sh",
        ":rook_deploy.jar",
    ],
    repository = "thesummit/rook",
    stamp = "@io_bazel_rules_docker//stamp:always",
    tags = ["arm64/v8"],
)

container_push(
  name = "github_push_latest",
  image = ":latest",
  format = "Docker",
  registry = "ghcr.io",
  repository = "thesummitdev/rook",
  tag = "latest",
)

container_push(
  name = "github_push_arm",
  image = ":arm",
  format = "Docker",
  registry = "ghcr.io",
  repository = "thesummitdev/rook",
  tag = "arm",
)

# The actual server binary. bazel run //:rook can run this locally.
java_binary(
    name = "rook",
    srcs = [
        "app/src/main/java/dev/thesummit/rook/RookApplication.java",
    ],
    main_class = "dev.thesummit.rook.RookApplication",
    resources =
        [
            ":web_bundle",
            "//app/src/main/resources",
            "//web/src/assets:static_assets",
        ],
    deps = [
        "//app/src/main/java/dev/thesummit/rook/auth",
        "//app/src/main/java/dev/thesummit/rook/utils:flag_module",
        "//app/src/main/java/dev/thesummit/rook/database:database_module",
        "//app/src/main/java/dev/thesummit/rook/database:schema_managers",
        "//app/src/main/java/dev/thesummit/rook/handlers",
        "//app/src/main/java/dev/thesummit/rook/models",
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
        ":rook",
        "//app/src/main/java/dev/thesummit/rook/database:database_module",
        "//app/src/main/java/dev/thesummit/rook/database:connection_pool",
        "//app/src/main/java/dev/thesummit/rook/database:database_service",
        "//app/src/main/java/dev/thesummit/rook/database:database_field",
        "//app/src/main/java/dev/thesummit/rook/auth",
        "//app/src/main/java/dev/thesummit/rook/models",
        "//app/src/main/java/dev/thesummit/rook/handlers",
        "//app/src/test/java/dev/thesummit/rook/handlers:LinkHandlerTest",
        "//app/src/test/java/dev/thesummit/rook/handlers:AuthHandlerTest",
        "//app/src/test/java/dev/thesummit/rook/handlers:TagHandlerTest",
        "//app/src/test/java/dev/thesummit/rook/handlers:UserHandlerTest",
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

# List of NPM packages that are required by the Angular application.
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
    "@npm//luxon",
]

# Build the Angular web application via the architect cli.
architect(
    name = "build",
    args = [
        "frontend:build",
        "--output-path=$(@D)",
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

# Rule that exposes the files for the java_binary resources attribute.
# Any files that need to end up in the jar from the web client need to be declared here.
genrule(
    name = "web_bundle",
    outs = [
        "web/index.html",
        "web/favicon.svg",
        "web/main.js",
        "web/main.js.map",
        "web/polyfills.js",
        "web/polyfills.js.map",
        "web/runtime.js",
        "web/runtime.js.map",
        "web/styles.css",
        "web/styles.css.map",
    ],
    cmd = """cp $(locations :build)/* $(RULEDIR)/web/.""",
    tools = [":build"],
    visibility = ["//visibility:private"],
)
