load("@rules_java//java:defs.bzl", "java_binary")
load("@bazel_common//tools/maven:pom_file.bzl", "pom_file")
load("@npm//@angular-devkit/architect-cli:index.bzl", "architect")
load("@io_bazel_rules_docker//container:container.bzl", "container_image")
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

# This defines a list of linux packages to download that will be installed in the container.
download_pkgs(
    name = "image_deps",
    image_tar = "@debian_stable_linux_amd64//image",
    packages = [
        "locales",
        "openjdk-11-jre",  # Only the Java runtime, JDK is not needed for production container.
        "postgresql",
        "postgresql-contrib",
        "sudo",  # Needed for the container_init script.
    ],
    visibility = ["//visibility:private"],
)

# Installs the downloaded packages in the base debian image container.
install_pkgs(
    name = "required_pkgs_image",
    image_tar = "@debian_stable_linux_amd64//image",
    installables_tar = ":image_deps.tar",
    output_image_name = "required_pkgs_image",
    visibility = ["//visibility:private"],
)

# Builds the latest version of the container image.
# blaze run //:latest will add the image to the local docker instance.
container_image(
    name = "latest",
    base = ":required_pkgs_image.tar",  # Our custom image base with the installed packages.
    creation_time = "{BUILD_TIMESTAMP}",
    entrypoint = [
        "/bin/bash",
        "-c",
        "./container_init.sh",
    ],
    env = {
        "POSTGRES_USER": "rook_system",
        "POSTGRES_PASSWORD": "rooksystem",
        "PGDATA":"/usr/local/pgsql/data",
        "LC_ALL":"en_US.UTF-8",
        "LANG":"en_US.UTF-8",
        "LANGUAGE":"en_US.UTF-8",
    },
    files = [
        "container_init.sh",
        "postgres/user_init.sql",
        "postgres/database_test_data.sql",
        "postgres/schema_init.sql",
        "postgres/populate.sql",
        ":rook_deploy.jar",
    ],
    ports = [
        "8000",  # Expose the 8000 port the server listens on.
    ],
    repository = "thesummit/rook",
    stamp = "@io_bazel_rules_docker//stamp:always",
    tags = ["latest"],
)

# The actual server binary. bazel run //:rook can run this locally, but a local postgres instance
# is required. See the postgres/* scripts for setting up a development database.
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
        "//app/src/main/java/dev/thesummit/rook/database:database_module",
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

# Rule that exposes the files for the java_binary resources attribute.
# Any files that need to end up in the jar from the web client need to be declared here.
genrule(
    name = "web_bundle",
    outs = [
        "web/index.html",
        "web/favicon.ico",
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
