package com.dioxuslabs.dioxus.runconfig

import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import org.rust.cargo.project.workspace.PackageOrigin
import org.rust.cargo.util.ArgCompleter
import org.rust.lang.core.completion.withPriority

private val boolCompleter = listOf("true", "false").map { LookupElementBuilder.create(it) }

private val optionalBoolCompleter: (DioxusCommandCompletionProvider) -> ArgCompleter? = { provider ->
    { ctx ->
        val l = provider
            .commonCommands
            .find { it.name == ctx.commandLinePrefix.firstOrNull() }
            ?.options
            ?.filter { it.long !in ctx.commandLinePrefix }
            ?.map { it.lookupElement }
            ?: emptyList()
        val l2 = mutableListOf(LookupElementBuilder.create("true").withPriority(1.0), LookupElementBuilder.create("false").withPriority(1.0))
        l2.addAll(l)
        l2
    }
}

interface Option {
    val name: String

    fun getCompleter(provider: DioxusCommandCompletionProvider): ArgCompleter? = null
    fun getLongName(): String
}

data class DioxusOption(override val name: String, val description: String, val completer: ArgCompleter? = null) : Option {
    override fun getLongName() = "--$name"
    override fun getCompleter(provider: DioxusCommandCompletionProvider): ArgCompleter? = completer
}

data class DioxusOptionWithFun(override val name: String, val description: String, val completer: (provider: DioxusCommandCompletionProvider) -> ArgCompleter?) : Option {
    override fun getLongName() = "--$name"

    // be careful to not cause an infinite loop
    override fun getCompleter(provider: DioxusCommandCompletionProvider): ArgCompleter? = completer(provider)
}

private val GENERAL_OPTIONS_SHORT = listOf(
    DioxusOption("verbose", "Use verbose output"),
    DioxusOption("trace", "Use trace output"),
    DioxusOption("json-output", "Output logs in JSON format"),
    DioxusOption("help", "Print help (see a summary with '-h')"),
)

private val GENERAL_OPTIONS = listOf(
    DioxusOptionWithFun(
        "fullstack",
        "Enable fullstack mode\nThis is automatically detected from `dx serve` if the \"fullstack\" feature is enabled by default.",
        optionalBoolCompleter
    ),
    DioxusOption("ssg", "Pre-render all routes returned from the app's `/static_routes` endpoint"),
) + GENERAL_OPTIONS_SHORT

private val RUN_OPTIONS = listOf(
    DioxusOption("port", "The port the server will run on") { emptyList() },
    DioxusOption("addr", "The address the server will run on") { emptyList() },
    DioxusOptionWithFun("open", "Open the app in the default browser [default: true - unless cli settings are set]", optionalBoolCompleter),
    DioxusOption("hot-reload", "Enable hot reloading [default: true - unless cli settings are set]") { boolCompleter },
    DioxusOption("always-on-top", "Configure always-on-top for desktop apps [default: true - unless cli settings are set]") { boolCompleter },
    DioxusOption("cross-origin-policy", "Set cross-origin-policy to same-origin"),
    DioxusOption("wsl-file-poll-interval", "Sets the interval in seconds that the CLI will poll for file changes on WSL") { emptyList() },
    DioxusOptionWithFun("interactive", "Run the server in interactive mode", optionalBoolCompleter),
    DioxusOption(
        "hot-patch",
        "Enable Rust hot-patching instead of full rebuilds\nThis is quite experimental and may lead to unexpected segfaults or crashes in development."
    ),
    DioxusOption("watch", "Watch the filesystem for changes and trigger a rebuild [default: true]") { boolCompleter },
    DioxusOption(
        "force-sequential",
        "This flag only applies to fullstack builds. By default fullstack builds will run the server and client builds in parallel. This flag will force the build to run the server build first, then the client build."
    ),
    DioxusOption("args", "Additional arguments to pass to the executable") { emptyList() },
)

private val RENDERER_OPTIONS = listOf(
    DioxusOption("web", "Enable the dioxus web renderer"),
    DioxusOption("webview", "Enable the dioxus webview renderer"),
    DioxusOption("native", "Enable the dioxus native renderer"),
    DioxusOption("server", "Enable the dioxus server renderer"),
    DioxusOption("liveview", "Enable the dioxus liveview renderer"),
)

private val TARGET_ALIAS_OPTIONS = listOf(
    DioxusOption("wasm", "Target the wasm triple"),
    DioxusOption("macos", "Target the macos triple"),
    DioxusOption("windows", "Target the windows triple"),
    DioxusOption("linux", "Target the linux triple"),
    DioxusOption("ios", "Target the ios triple"),
    DioxusOption("android", "Target the android triple"),
    DioxusOption("host", "Target the host triple"),
    DioxusOption("desktop", "Target the desktop triple"),
)

private val TARGET_OPTIONS = listOf(
    DioxusOption("bundle", "The bundle format to target for the build: supports web, macos, windows, linux, ios, android, and server") {
        listOf("web", "macos", "windows", "linux", "ios", "android", "server").map { LookupElementBuilder.create(it) }
    },
    DioxusOption(
        "platform",
        "Build platform: supports Web, MacOS, Windows, Linux, iOS, Android, and Server\n" +
                "The platform implies a combination of the target alias, renderer, and bundle format flags.\n" +
                "You should generally prefer to use the `--web`, `--webview`, or `--native` flags to set the renderer or the `--wasm`, `--macos`, `--windows`, `--linux`, `--ios`, or `--android` flags to set the target alias instead of this flag. The renderer, target alias, and bundle format will be inferred if you only pass one."
    ) { listOf("web", "macos", "windows", "linux", "ios", "android", "server", "liveview").map { LookupElementBuilder.create(it) } },
    DioxusOption("release", "Build in release mode"),
    DioxusOption("package", "The package to build") { ctx ->
        ctx.currentWorkspace?.packages.orEmpty().map {
            val priority = if (it.origin == PackageOrigin.WORKSPACE) 1.0 else 0.0
            LookupElementBuilder.create(it.name).withPriority(priority)
        }
    }, // This has a copy
    DioxusOption("bin", "Build a specific binary") { emptyList() }, // TODO: binary could be autocompleted
    DioxusOption("example", "Build a specific example") { emptyList() }, // TODO: examples could be autocompleted
    DioxusOption("profile", "Build the app with custom a profile") { emptyList() }, // TODO: profiles could be autocompleted
    DioxusOption("features", "Space separated list of features to activate") { emptyList() }, // TODO: features could be autocompleted
    DioxusOption("no-default-features", "Don't include the default features in the build"),
    DioxusOption("all-features", "Include all features in the build"),
    DioxusOption("target", "Rustc platform triple") { ctx ->
        ctx.projects.firstOrNull()?.rustcInfo?.rustcTargets?.map { LookupElementBuilder.create(it) } ?: emptyList()
    },
    DioxusOption(
        "cargo-args",
        "Extra arguments passed to `cargo`\n" +
                "To see a list of args, run `cargo rustc --help`\n" +
                "This can include stuff like, \"--locked\", \"--frozen\", etc. Note that `dx` sets many of these args directly from other args in this command."
    ) { emptyList() },
    DioxusOption(
        "rustc-args",
        "Extra arguments passed to `rustc`. This can be used to customize the linker, or other flags.\n" +
                "For example, specifign `dx build --rustc-args \"-Clink-arg=-Wl,-blah\"` will pass \"-Clink-arg=-Wl,-blah\" to the underlying the `cargo rustc` command:\n" +
                "cargo rustc -- -Clink-arg=-Wl,-blah"
    ) { emptyList() },
    DioxusOption("skip-assets", "Skip collecting assets from dependencies"),
    DioxusOptionWithFun(
        "inject-loading-scripts",
        "Inject scripts to load the wasm and js files for your dioxus app if they are not already present [default: true]",
        optionalBoolCompleter
    ),
    DioxusOption("wasm-split", "Experimental: Bundle split the wasm binary into multiple chunks based on `#[wasm_split]` annotations"),
    DioxusOptionWithFun(
        "debug-symbols",
        "Generate debug symbols for the wasm binary [default: true]\n" +
                "This will make the binary larger and take longer to compile, but will allow you to debug the wasm binary",
        optionalBoolCompleter
    ),
    DioxusOption("device", "Are we building for a device or just the simulator. If device is false, then we'll build for the simulator"),
    DioxusOption(
        "base-path",
        "The base path the build will fetch assets relative to. This will override the base path set in the `dioxus` config"
    ) { emptyList() }, // TODO: path could be autocompleted
    DioxusOption(
        "apple-entitlements",
        "The path to the Apple entitlements file to used to sign the resulting app bundle.\n" +
                "On iOS, this is required for deploy to a device and some configurations in the simulator."
    ) { emptyList() }, // TODO: filepath could be autocompleted
    DioxusOption(
        "apple-team-id",
        "The Apple team ID to use when signing the app bundle.\n" +
                "Usually this is an email or name associated with your Apple Developer account, usually in the format `Signing Name (GXTEAMID123)`.\n" +
                "This is passed directly to the `codesign` tool."
    ) { emptyList() },
)

private val LOGGING_OPTIONS = listOf(
    DioxusOption("log-to-file", "Write *all* logs to a file") { emptyList() }, // TODO: filepath could be autocompleted
)

private val MANIFEST_OPTIONS = listOf(
    DioxusOption("locked", "Assert that `Cargo.lock` will remain unchanged"),
    DioxusOption("offline", "Run without accessing the network"),
    DioxusOption("frozen", "Equivalent to specifying both --locked and --offline"),
)

enum class DioxusCommands(val description: String, val options: List<Option>) {
    SERVE(
        " Build, watch, and serve the project",
        RUN_OPTIONS +
                GENERAL_OPTIONS +
                TARGET_ALIAS_OPTIONS +
                RENDERER_OPTIONS +
                TARGET_OPTIONS +
                LOGGING_OPTIONS +
                MANIFEST_OPTIONS
    ),
    BUNDLE(
        "Bundle the Dioxus app into a shippable object",
        listOf(
            DioxusOption("package-types", "The package types to bundle") {
                listOf("macos", "ios", "msi", "nsis", "deb", "rpm", "appimage", "dmg", "updater").map { LookupElementBuilder.create(it) }
            },
            DioxusOption(
                "out-dir",
                "The directory in which the final bundle will be placed.\n" +
                        "Relative paths will be placed relative to the current working directory if specified. Otherwise, the out_dir path specified in Dioxus.toml will be used (relative to the crate root).\n" +
                        "We will flatten the artifacts into this directory - there will be no differentiation between artifacts produced by different platforms."
            ) { emptyList() }, // TODO: out path could be autocompleted
        ) +
                GENERAL_OPTIONS +
                TARGET_ALIAS_OPTIONS +
                RENDERER_OPTIONS +
                TARGET_OPTIONS +
                LOGGING_OPTIONS +
                MANIFEST_OPTIONS
    ),
    BUILD(
        "Build the Dioxus project and all of its assets",
        GENERAL_OPTIONS +
                TARGET_ALIAS_OPTIONS +
                RENDERER_OPTIONS +
                TARGET_OPTIONS +
                LOGGING_OPTIONS +
                MANIFEST_OPTIONS
    ),
    RUN(
        "Run the project without any hotreloading",
        RUN_OPTIONS +
                GENERAL_OPTIONS +
                TARGET_ALIAS_OPTIONS +
                RENDERER_OPTIONS +
                TARGET_OPTIONS +
                LOGGING_OPTIONS +
                MANIFEST_OPTIONS
    ),
    DOCTOR(
        "Diagnose installed tools and system configuration",
        GENERAL_OPTIONS_SHORT + LOGGING_OPTIONS + MANIFEST_OPTIONS
    ),
    TRANSLATE(
        "Translate a source file into Dioxus code",
        listOf(
            DioxusOption("component", "Activate debug mode"),
            DioxusOption("file", "Input file") { emptyList() }, // TODO: input file could be autocompleted
            DioxusOption("raw", "Input file") { emptyList() }, // TODO: raw input file could be autocompleted
            DioxusOption("output", "Output file, stdout if not present") { emptyList() }, // TODO: output file could be autocompleted
        ) + GENERAL_OPTIONS_SHORT + LOGGING_OPTIONS + MANIFEST_OPTIONS
    ),
    FMT(
        "Automatically format RSX",
        listOf(
            DioxusOption("all-code", "Format rust code before the formatting the rsx macros"),
            DioxusOption("check", "Run in 'check' mode. Exits with 0 if input is formatted correctly. Exits with 1 and prints a diff if formatting is required"),
            DioxusOption("raw", "Input rsx (selection)") { emptyList() },
            DioxusOption("file", "Input file at path (set to \"-\" to read file from stdin, and output formatted file to stdout)") { emptyList() },
            DioxusOption("split-line-attributes", "Split attributes in lines or not"),
            DioxusOption("package", "The package to build") { ctx ->
                ctx.currentWorkspace?.packages.orEmpty().map {
                    val priority = if (it.origin == PackageOrigin.WORKSPACE) 1.0 else 0.0
                    LookupElementBuilder.create(it.name).withPriority(priority)
                }
            } // This has a copy
        ) + GENERAL_OPTIONS_SHORT + LOGGING_OPTIONS + MANIFEST_OPTIONS
    ),
    CHECK(
        "Check the project for any issues",
        listOf(
            DioxusOption("file", "Input file") { emptyList() }, // TODO: input file could be autocompleted
        ) +
                GENERAL_OPTIONS +
                TARGET_ALIAS_OPTIONS +
                RENDERER_OPTIONS +
                TARGET_OPTIONS +
                LOGGING_OPTIONS +
                MANIFEST_OPTIONS

    ),
    CONFIG(
        "Dioxus config file controls",
        listOf(
            DioxusOption("init", "Init `Dioxus.toml` for project/folder"),
            DioxusOption("format-print", "Format print Dioxus config"),
            DioxusOption("custom-html", "Create a custom html file"),
            DioxusOption("set", "Set CLI settings"),
            DioxusOption("help", "Print this message or the help of the given subcommand(s)"),
        ) + GENERAL_OPTIONS_SHORT + LOGGING_OPTIONS + MANIFEST_OPTIONS
    ),
    SELF_UPDATE(
        "Update the Dioxus CLI to the latest version",
        listOf(
            DioxusOption("nightly", "Use the latest nightly build"),
            DioxusOption("version", "Specify a version to install") { emptyList() }, // TODO: version maybe could be autocompleted
            DioxusOptionWithFun("install", "Install the update [default: true]", optionalBoolCompleter),
            DioxusOption("list", "List available versions"),
            DioxusOption("force", "Force the update even if the current version is up to date"),
        ) + GENERAL_OPTIONS_SHORT + LOGGING_OPTIONS + MANIFEST_OPTIONS
    ),
    TOOLS(
        "Run a dioxus build tool. IE `build-assets`, etc",
        GENERAL_OPTIONS_SHORT + LOGGING_OPTIONS + MANIFEST_OPTIONS
    );

    val presentableName: String get() = name.lowercase().replace('_', '-')
}

