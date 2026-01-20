package com.github.hirofumi.title2025

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.openapi.wm.ex.ToolWindowManagerListener
import com.intellij.terminal.frontend.toolwindow.TerminalTabsManagerListener
import com.intellij.terminal.frontend.toolwindow.TerminalToolWindowTab
import com.intellij.terminal.frontend.toolwindow.TerminalToolWindowTabsManager
import org.jetbrains.plugins.terminal.TerminalToolWindowFactory.TOOL_WINDOW_ID

internal class TerminalToolWindowManagerListener(
    private val project: Project,
) : ToolWindowManagerListener {
    override fun toolWindowsRegistered(
        ids: List<String?>,
        toolWindowManager: ToolWindowManager,
    ) {
        if (!ids.contains(TOOL_WINDOW_ID)) return
        val toolWindow = toolWindowManager.getToolWindow(TOOL_WINDOW_ID) ?: return

        ApplicationManager.getApplication().invokeLater {
            if (project.isDisposed) return@invokeLater

            val titleSyncService = project.service<TerminalTabTitleSyncService>()
            val tabsManager = TerminalToolWindowTabsManager.getInstance(project)

            tabsManager.addListener(
                toolWindow.disposable,
                object : TerminalTabsManagerListener {
                    override fun tabAdded(tab: TerminalToolWindowTab) = titleSyncService.bind(tab)
                },
            )

            for (tab in tabsManager.tabs) {
                titleSyncService.bind(tab)
            }
        }
    }
}
