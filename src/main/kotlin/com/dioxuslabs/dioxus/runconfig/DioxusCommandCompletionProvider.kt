package com.dioxuslabs.dioxus.runconfig

import org.rust.cargo.project.model.CargoProjectsService
import org.rust.cargo.project.workspace.CargoWorkspace
import org.rust.cargo.util.CmdBase
import org.rust.cargo.util.Opt
import org.rust.cargo.util.OptBuilder
import org.rust.cargo.util.RsCommandCompletionProvider

class DioxusCommandCompletionProvider(projects: CargoProjectsService, implicitTextPrefix: String, workspaceGetter: () -> CargoWorkspace?) :
    RsCommandCompletionProvider(projects, implicitTextPrefix, workspaceGetter) {

    constructor(projects: CargoProjectsService, workspaceGetter: () -> CargoWorkspace?) : this(projects, "", workspaceGetter)

    constructor(projects: CargoProjectsService, workspace: CargoWorkspace?) : this(projects, { workspace })

    public override val commonCommands: List<CmdBase> = buildList {
        for (command in DioxusCommands.entries) {
            Cmd(command.presentableName) {
                for (option in command.options) {
                    val completer = option.getCompleter(this@DioxusCommandCompletionProvider)
                    if (completer != null) {
                        opt(option.name, completer)
                    } else {
                        flag(option.name)
                    }
                }
            }.also { add(it) }
        }
    }

    private class Cmd(name: String, initOptions: DioxusOptBuilder.() -> Unit = {}) : CmdBase(name) {
        override val options: List<Opt> = DioxusOptBuilder().apply(initOptions).result
    }

    private class DioxusOptBuilder(override val result: MutableList<Opt> = mutableListOf()) : OptBuilder
}
