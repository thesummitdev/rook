load("@build_bazel_rules_nodejs//:index.bzl", "nodejs_binary")

package(default_visibility = ["//visibility:public"])

# Custom ts_library compiler that runs tsc_wrapped with angular/compiler-cli statically linked
# This can be used with worker mode because we don't need the linker at runtime to make
# the angular plugin loadable
# Just a clone of @npm//@bazel/typescript/bin:tsc_wrapped with added deps
nodejs_binary(
    name = "tsc_wrapped_with_angular",
    data = [
        "@npm//@angular/compiler-cli",
        "@npm//@bazel/typescript",
    ],
    entry_point = "@npm//:node_modules/@bazel/typescript/internal/tsc_wrapped/tsc_wrapped.js",
    # TODO: turn on --worker_sandboxing and remove this flag to see failure to load the plugin
    templated_args = ["--bazel_patch_module_resolver"],
    visibility = ["//:__subpackages__"],
)
