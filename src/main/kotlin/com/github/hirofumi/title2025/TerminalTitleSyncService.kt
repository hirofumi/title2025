package com.github.hirofumi.title2025

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.EDT
import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.options.advanced.AdvancedSettings
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.terminal.frontend.toolwindow.TerminalToolWindowTab
import com.intellij.terminal.frontend.view.impl.TerminalViewImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.plugins.terminal.TerminalEngine
import org.jetbrains.plugins.terminal.TerminalOptionsProvider
import org.jetbrains.plugins.terminal.block.reworked.TerminalSessionModelImpl
import java.util.Collections
import java.util.WeakHashMap

private val LOG = logger<TerminalTitleSyncService>()

@Service(Service.Level.PROJECT)
class TerminalTitleSyncService(
    private val project: Project,
    private val scope: CoroutineScope,
) {
    private val boundTabs = Collections.newSetFromMap(WeakHashMap<TerminalToolWindowTab, Boolean>())

    fun bind(tab: TerminalToolWindowTab) {
        if (TerminalOptionsProvider.instance.terminalEngine != TerminalEngine.REWORKED) return
        if (!AdvancedSettings.getBoolean("terminal.show.application.title")) return

        ApplicationManager.getApplication().invokeLater {
            if (!boundTabs.add(tab)) return@invokeLater
            if (!tab.content.isValid || project.isDisposed) return@invokeLater

            val terminalViewImpl = tab.view as? TerminalViewImpl
            if (terminalViewImpl == null) {
                LOG.warn("tab.view is not TerminalViewImpl: ${tab.view::class.qualifiedName}")
                return@invokeLater
            }

            val sessionModel = terminalViewImpl.sessionModel as? TerminalSessionModelImpl
            if (sessionModel == null) {
                LOG.warn("sessionModel is not TerminalSessionModelImpl: ${terminalViewImpl.sessionModel::class.qualifiedName}")
                return@invokeLater
            }

            val job =
                scope.launch {
                    sessionModel.terminalState
                        .map { state -> state.windowTitle }
                        .distinctUntilChanged()
                        .drop(1) // avoid overwriting the default tab name
                        .conflate()
                        .collect { title ->
                            withContext(Dispatchers.EDT) {
                                if (!tab.content.isValid || project.isDisposed) return@withContext
                                tab.content.displayName = title
                            }
                        }
                }

            Disposer.register(tab.content) { job.cancel() }
        }
    }
}
