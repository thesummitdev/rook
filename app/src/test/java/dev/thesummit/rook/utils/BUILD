load("//:app/src/test/junit5.bzl", "java_junit5_test")

package(default_visibility = ["//visibility:public"])

java_junit5_test(
    name = "FlagServiceTest",
    srcs = ["FlagServiceTest.java"],
    test_package = "dev.thesummit.rook",
    use_testrunner = False,
    deps = [
        "//app/src/main/java/dev/thesummit/rook/utils:flag_service",
        "@maven//:org_junit_jupiter_junit_jupiter_api",
    ],
)

