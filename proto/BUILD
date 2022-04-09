load("//tools/build/bazel:osgi_java_library.bzl", "osgi_proto_jar")

PROTOS = [
    ":HierarchicalMaster_proto"
]

osgi_proto_jar(
    name = "HierarchicalMasterServices",
    grpc_proto_lib = ":HierarchicalMaster_proto",
    deps = [
            "@com_google_api_grpc_proto_google_common_protos//jar",
        ],
    proto_libs = PROTOS,
)

proto_library(
    name = "HierarchicalMaster_proto",
    srcs = ["Hierarchical.proto"],
    deps = [
            "@com_google_protobuf//:any_proto",
            "@com_google_protobuf//:descriptor_proto",
        ],
)

