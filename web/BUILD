load("@npm//@bazel/typescript:index.bzl", "ts_project")

package(default_visibility = ["//visibility:public"])

ts_project(
    name = "bundle",
    # glob is a quick way to select all the code,
    # but has performance penalty in that Bazel must evaluate it.
    #srcs = glob(["main.ts"]),
    srcs = ["main.ts"],
    incremental = True,
    source_map = True,
    tsconfig = "tsconfig.json",
    deps = [],
)

filegroup(
    name = "static_files",
    srcs = [
        "index.html",
        "//web/styles",
    ],
)
