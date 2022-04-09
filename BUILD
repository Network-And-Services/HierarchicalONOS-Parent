BUNDLES = [
    "//core/protobuf/models:onos-core-protobuf-models",
    "//core/protobuf/models/proto:onos-core-protobuf-models-proto",
    "//apps/hierarchical-sync-master/api:onos-apps-hierarchical-sync-master-api",
    "//apps/hierarchical-sync-master/app:onos-apps-hierarchical-sync-master-app",
    "//apps/hierarchical-sync-master/proto:HierarchicalMasterServices",
]

onos_app(
    category = "Integrations",
    included_bundles = BUNDLES,
    title = "Hierarchical Sync Master",
    required_apps = [
            "org.onosproject.protocols.grpc",
        ],
    url = "http://onosproject.org",
)