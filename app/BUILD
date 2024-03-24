COMPILE_DEPS = CORE_DEPS + JACKSON + REST + [
    "@io_grpc_grpc_java//api",
    "@io_grpc_grpc_java//netty",
    "@io_grpc_grpc_java//stub",
    "//deps:com_google_protobuf_protobuf_java",
    "@jaxb_api//jar",
    "//core/store/serializers:onos-core-serializers",
    "//core/protobuf/models:onos-core-protobuf-models",
    "//core/protobuf/models/proto:onos-core-protobuf-models-proto",
    "//apps/hierarchical-sync-master/api:onos-apps-hierarchical-sync-master-api",
    "//apps/hierarchical-sync-master/proto:HierarchicalMasterServices",
]

osgi_jar_with_tests(
    api_description = "REST API for Hierarchical Sync App",
    api_package = "org.onosproject.hierarchicalsyncmaster.rest",
    api_title = "Hierarchical",
    api_version = "1.0",
    web_context = "/onos/hierarchical",
    test_deps = TEST_ADAPTERS,
    deps = COMPILE_DEPS,
)
