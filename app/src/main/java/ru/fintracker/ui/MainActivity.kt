package ru.fintracker.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import ru.fintracker.R
import ru.fintracker.databinding.ActivityMainBinding
import ru.fintracker.ui.categories.CategoriesFragment
import ru.fintracker.ui.dashboard.DashboardFragment
import ru.fintracker.ui.settings.SettingsFragment
import ru.fintracker.ui.stats.StatsFragment
import ru.fintracker.ui.transactions.TransactionsFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val notificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.bottomNav.setOnItemSelectedListener { item ->
            openTab(item.itemId)
            true
        }

        if (savedInstanceState == null) {
            binding.bottomNav.selectedItemId = R.id.nav_dashboard
        } else if (supportFragmentManager.findFragmentById(R.id.fragmentContainer) == null) {
            openTab(R.id.nav_dashboard)
            binding.bottomNav.selectedItemId = R.id.nav_dashboard
        }
        requestNotificationPermission()
    }

    fun selectTab(itemId: Int) {
        binding.bottomNav.selectedItemId = itemId
    }

    private fun openTab(itemId: Int): Boolean {
        val tag = itemId.toString()
        if (supportFragmentManager.findFragmentById(R.id.fragmentContainer)?.tag == tag) {
            return true
        }
        val fragment = supportFragmentManager.findFragmentByTag(tag) ?: createFragment(itemId) ?: return false
        supportFragmentManager.commit {
            setReorderingAllowed(true)
            replace(R.id.fragmentContainer, fragment, tag)
        }
        return true
    }

    private fun createFragment(itemId: Int): Fragment? = when (itemId) {
        R.id.nav_dashboard -> DashboardFragment()
        R.id.nav_transactions -> TransactionsFragment()
        R.id.nav_stats -> StatsFragment()
        R.id.nav_categories -> CategoriesFragment()
        R.id.nav_settings -> SettingsFragment()
        else -> null
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        val granted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
        if (!granted) {
            notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}
