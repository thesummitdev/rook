load("//:app/src/test/junit5.bzl", "java_junit5_test")

package(default_visibility = ["//visibility:public"])

java_junit5_test(
    name = "LinkHandlerTest",
    srcs = ["LinkHandlerTest.java"],
    test_package = "dev.thesummit.flink",
    use_testrunner = False,
    deps = [
        "//app/src/main/java/dev/thesummit/flink/database:database_service",
        "//app/src/main/java/dev/thesummit/flink/handlers",
        "//app/src/main/java/dev/thesummit/flink/models",
        "@maven//:io_javalin_javalin",
        "@maven//:org_json_json",
        "@maven//:org_junit_jupiter_junit_jupiter_api",
        "@maven//:org_mockito_mockito_core",
        "@maven//:org_mockito_mockito_junit_jupiter",
    ],
)

java_junit5_test(
    name = "TagHandlerTest",
    srcs = ["TagHandlerTest.java"],
    test_package = "dev.thesummit.flink",
    use_testrunner = False,
    deps = [
        "//app/src/main/java/dev/thesummit/flink/database:database_service",
        "//app/src/main/java/dev/thesummit/flink/handlers",
        "//app/src/main/java/dev/thesummit/flink/models",
        "@maven//:io_javalin_javalin",
        "@maven//:org_json_json",
        "@maven//:org_junit_jupiter_junit_jupiter_api",
        "@maven//:org_mockito_mockito_core",
        "@maven//:org_mockito_mockito_junit_jupiter",
    ],
)

java_junit5_test(
    name = "PreferenceHandlerTest",
    srcs = ["PreferenceHandlerTest.java"],
    test_package = "dev.thesummit.flink",
    use_testrunner = False,
    deps = [
        "//app/src/main/java/dev/thesummit/flink/database:database_service",
        "//app/src/main/java/dev/thesummit/flink/handlers",
        "//app/src/main/java/dev/thesummit/flink/models",
        "@maven//:io_javalin_javalin",
        "@maven//:org_json_json",
        "@maven//:org_junit_jupiter_junit_jupiter_api",
        "@maven//:org_mockito_mockito_core",
        "@maven//:org_mockito_mockito_junit_jupiter",
    ],
)

java_junit5_test(
    name = "UserHandlerTest",
    srcs = ["UserHandlerTest.java"],
    test_package = "dev.thesummit.flink",
    use_testrunner = False,
    deps = [
        "//app/src/main/java/dev/thesummit/flink/auth",
        "//app/src/main/java/dev/thesummit/flink/database:database_service",
        "//app/src/main/java/dev/thesummit/flink/handlers",
        "//app/src/main/java/dev/thesummit/flink/models",
        "@maven//:io_javalin_javalin",
        "@maven//:org_json_json",
        "@maven//:org_junit_jupiter_junit_jupiter_api",
        "@maven//:org_mockito_mockito_core",
        "@maven//:org_mockito_mockito_junit_jupiter",
    ],
)

java_junit5_test(
    name = "AuthHandlerTest",
    srcs = ["AuthHandlerTest.java"],
    test_package = "dev.thesummit.flink",
    use_testrunner = False,
    deps = [
        "//app/src/main/java/dev/thesummit/flink/auth",
        "//app/src/main/java/dev/thesummit/flink/database:database_service",
        "//app/src/main/java/dev/thesummit/flink/handlers",
        "//app/src/main/java/dev/thesummit/flink/models",
        "@maven//:com_auth0_java_jwt",
        "@maven//:io_javalin_javalin",
        "@maven//:org_json_json",
        "@maven//:org_junit_jupiter_junit_jupiter_api",
        "@maven//:org_mockito_mockito_core",
        "@maven//:org_mockito_mockito_junit_jupiter",
    ],
)
