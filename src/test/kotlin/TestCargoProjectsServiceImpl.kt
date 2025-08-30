package org.rust.cargo.project.model.impl

import com.intellij.openapi.project.Project

// This is here as workaround for https://youtrack.jetbrains.com/issue/RUST-16312
class TestCargoProjectsServiceImpl(project: Project) : CargoProjectsServiceImpl(project)