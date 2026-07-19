package com.nxteam.nxtool

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build

enum class McSource { PLAY_STORE, SIDELOADED, UNKNOWN_SOURCE, NOT_INSTALLED }

data class McStatus(
    val source: McSource,
    val versionName: String?
) {
    val installed: Boolean get() = source != McSource.NOT_INSTALLED
    val fromPlayStore: Boolean get() = source == McSource.PLAY_STORE

    val label: String
        get() = when (source) {
            McSource.PLAY_STORE -> "Play Store  ${versionName ?: ""}".trim()
            McSource.SIDELOADED -> "Sideloaded  ${versionName ?: ""}".trim()
            McSource.UNKNOWN_SOURCE -> "Unknown source  ${versionName ?: ""}".trim()
            McSource.NOT_INSTALLED -> "Not installed"
        }
}

object McDetector {

    const val MC_PACKAGE = "com.mojang.minecraftpe"
    private const val PLAY_STORE_PACKAGE = "com.android.vending"

    fun status(context: Context): McStatus {
        val pm = context.packageManager
        val versionName = try {
            pm.getPackageInfo(MC_PACKAGE, 0).versionName
        } catch (e: PackageManager.NameNotFoundException) {
            return McStatus(McSource.NOT_INSTALLED, null)
        }

        val installer = try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                pm.getInstallSourceInfo(MC_PACKAGE).installingPackageName
            } else {
                @Suppress("DEPRECATION")
                pm.getInstallerPackageName(MC_PACKAGE)
            }
        } catch (e: Exception) {
            null
        }

        val source = when {
            installer == PLAY_STORE_PACKAGE -> McSource.PLAY_STORE
            installer == null -> McSource.SIDELOADED
            else -> McSource.UNKNOWN_SOURCE
        }
        return McStatus(source, versionName)
    }

    fun launchMinecraft(context: Context): Boolean {
        val intent = context.packageManager.getLaunchIntentForPackage(MC_PACKAGE) ?: return false
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
        return true
    }

    fun openPlayStore(context: Context) {
        val market = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$MC_PACKAGE"))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            context.startActivity(market)
        } catch (e: Exception) {
            context.startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=$MC_PACKAGE")
                ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        }
    }
}
