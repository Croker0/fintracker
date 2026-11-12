package ru.fintracker.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import kotlinx.coroutines.launch
import ru.fintracker.FinTrackerApp
import ru.fintracker.R
import ru.fintracker.core.Dates
import ru.fintracker.data.export.BackupManager
import ru.fintracker.data.export.CsvExporter
import ru.fintracker.data.export.FileSharer
import ru.fintracker.databinding.FragmentSettingsBinding
import ru.fintracker.settings.AppSettings
import ru.fintracker.ui.common.collectFlow
import ru.fintracker.ui.common.showIf
import ru.fintracker.work.WorkScheduler
import java.util.Locale

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private val app: FinTrackerApp
        get() = requireActivity().application as FinTrackerApp

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.rowReminderTime.setOnClickListener {
            val current = app.container.settings.state.value
            showTimePicker(current.reminderHour, current.reminderMinute)
        }
        binding.rowExportCsv.setOnClickListener { exportCsv() }
        binding.rowExportJson.setOnClickListener { exportJson() }
        binding.rowSeedDemo.setOnClickListener { confirmSeedDemo() }
        binding.rowClearData.setOnClickListener { confirmClear() }

        collectFlow(app.container.settings.state) { render(it) }
    }

    private fun render(settings: AppSettings) {
        binding.switchReminder.setOnCheckedChangeListener(null)
        binding.switchReminder.isChecked = settings.reminderEnabled
        binding.switchReminder.setOnCheckedChangeListener { _, checked ->
            app.container.settings.setReminderEnabled(checked)
            if (checked) {
                WorkScheduler.scheduleReminder(
                    requireContext(),
                    settings.reminderHour,
                    settings.reminderMinute
                )
            } else {
                WorkScheduler.cancelReminder(requireContext())
            }
        }

        binding.tvReminderTime.text =
            String.format(Locale.US, "%02d:%02d", settings.reminderHour, settings.reminderMinute)

        binding.switchSync.setOnCheckedChangeListener(null)
        binding.switchSync.isChecked = settings.autoSync
        binding.switchSync.setOnCheckedChangeListener { _, checked ->
            app.container.settings.setAutoSync(checked)
            if (checked) {
                WorkScheduler.scheduleSync(requireContext())
            } else {
                WorkScheduler.cancelSync(requireContext())
            }
        }

        binding.tvLastSync.text = if (settings.lastSyncAt == 0L) {
            "Резервная копия ещё не создавалась"
        } else {
            "Последняя синхронизация: ${Dates.dateTime(settings.lastSyncAt)}"
        }
    }

    private fun showTimePicker(hour: Int, minute: Int) {
        val picker = MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_24H)
            .setHour(hour)
            .setMinute(minute)
            .setTitleText("Время напоминания")
            .build()

        picker.addOnPositiveButtonClickListener {
            app.container.settings.setReminderTime(picker.hour, picker.minute)
            val settings = app.container.settings.state.value
            if (settings.reminderEnabled) {
                WorkScheduler.scheduleReminder(requireContext(), picker.hour, picker.minute)
            }
        }
        picker.show(childFragmentManager, "reminder_time_picker")
    }

    private fun exportCsv() {
        viewLifecycleOwner.lifecycleScope.launch {
            runCatching {
                CsvExporter.export(requireContext(), app.container.repository)
            }.onSuccess { file ->
                FileSharer.share(
                    requireContext(),
                    file,
                    "text/csv",
                    getString(R.string.share_report_chooser)
                )
            }.onFailure {
                toast(getString(R.string.export_error))
            }
        }
    }

    private fun exportJson() {
        viewLifecycleOwner.lifecycleScope.launch {
            runCatching {
                BackupManager.exportToCache(requireContext(), app.container.repository)
            }.onSuccess { file ->
                FileSharer.share(
                    requireContext(),
                    file,
                    "application/json",
                    "Резервная копия FinTracker"
                )
            }.onFailure {
                toast(getString(R.string.export_error))
            }
        }
    }

    private fun confirmSeedDemo() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Загрузить демо-данные?")
            .setMessage("Текущие операции и пользовательские категории будут заменены примером.")
            .setNegativeButton("Отмена", null)
            .setPositiveButton("Загрузить") { _, _ ->
                viewLifecycleOwner.lifecycleScope.launch {
                    app.container.repository.seedDemoData(force = true)
                    toast("Демо-данные загружены")
                }
            }
            .show()
    }

    private fun confirmClear() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Удалить все операции?")
            .setMessage("Категории и настройки останутся, операции будут удалены безвозвратно.")
            .setNegativeButton("Отмена", null)
            .setPositiveButton("Удалить") { _, _ ->
                viewLifecycleOwner.lifecycleScope.launch {
                    app.container.repository.clearTransactions()
                    toast("Операции удалены")
                }
            }
            .show()
    }

    private fun toast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
