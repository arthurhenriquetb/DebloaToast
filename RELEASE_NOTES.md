# DebloaToast Expert v0.12

**New recovery scan:** Find factory/system packages uninstalled for Android user 0 directly from Android's package manager, even when DebloaToast's local removal history is empty. Review detected packages, uncheck any unwanted packages, and reinstall selected packages via `cmd package install-existing --user 0` over authorized ADB.

**Important:** Only packages still present in the firmware can be reinstalled this way. Restoring packages may change system behavior, and some repairs require a restart. The Expert removal feature remains high risk and may render a device unusable.

Developer: Arthur Henrique Tostes Batista Aranha.
