load("//tools/build/bazel:osgi_java_library.bzl", "osgi_proto_jar")

PROTOS = [
    ":HierarchicalMaster_proto"
]

osgi_proto_jar(
    name = "HierarchicalMasterServices",
    grpc_proto_lib = ":HierarchicalMaster_proto",
    proto_libs = PROTOS,
    deps = ["//core/protobuf/models/proto:onos-core-protobuf-models-proto"]
)

proto_library(
    name = "HierarchicalMaster_proto",
    srcs = ["Hierarchical.proto"],
    deps = ["//core/protobuf/models/proto:DeviceEvent_proto", "//core/protobuf/models/proto:LinkEvent_proto"]
)

