package com.biblia.app.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.DriveFileRenameOutline
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SettingsInputAntenna
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.TextSnippet
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiTethering
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * One place to look up the Material icon standing in for each glyph that used
 * to be a raw emoji `Text(...)`. Keeps the "real icons, not emoji" look
 * consistent everywhere instead of picking icons ad hoc per screen.
 */
object PulseIcons {
    val Send: ImageVector = Icons.AutoMirrored.Filled.Send
    val Receive: ImageVector = Icons.Filled.Download
    val Broadcasting: ImageVector = Icons.Filled.SettingsInputAntenna
    val Discovering: ImageVector = Icons.Filled.WifiTethering
    val SignalBars: ImageVector = Icons.Filled.Wifi
    val Device: ImageVector = Icons.Filled.PhoneAndroid
    val TargetPin: ImageVector = Icons.Filled.LocationOn
    val EmptyInbox: ImageVector = Icons.Filled.Inbox
    val Camera: ImageVector = Icons.Filled.PhotoCamera
    val FolderClosed: ImageVector = Icons.Filled.Folder
    val FolderOpenEmpty: ImageVector = Icons.Filled.FolderOpen
    val AppPackage: ImageVector = Icons.Filled.Inventory2
    val GenericFile: ImageVector = Icons.AutoMirrored.Filled.InsertDriveFile
    val ArchiveFile: ImageVector = Icons.Filled.Archive
    val DocFile: ImageVector = Icons.Filled.Description
    val Warning: ImageVector = Icons.Filled.Warning
    val Photo: ImageVector = Icons.Filled.Image
    val Video: ImageVector = Icons.Filled.Movie
    val Audio: ImageVector = Icons.Filled.MusicNote
    val Brand: ImageVector = Icons.Filled.MenuBook

    // File manager
    val GridViewIcon: ImageVector = Icons.Filled.GridView
    val ListViewIcon: ImageVector = Icons.Filled.ViewList
    val Sort: ImageVector = Icons.Filled.Sort
    val Search: ImageVector = Icons.Filled.Search
    val Copy: ImageVector = Icons.Filled.ContentCopy
    val Cut: ImageVector = Icons.Filled.ContentCut
    val Paste: ImageVector = Icons.Filled.ContentPaste
    val Delete: ImageVector = Icons.Filled.Delete
    val Rename: ImageVector = Icons.Filled.DriveFileRenameOutline
    val NewFolder: ImageVector = Icons.Filled.CreateNewFolder
    val ShareIcon: ImageVector = Icons.Filled.Share
    val InfoIcon: ImageVector = Icons.Filled.Info
    val MoreIcon: ImageVector = Icons.Filled.MoreVert
    val Apps: ImageVector = Icons.Filled.Apps
    val PdfFile: ImageVector = Icons.Filled.PictureAsPdf
    val TextFile: ImageVector = Icons.Filled.TextSnippet

    /** File-category icon, mirrors the switch that used to pick an emoji string. */
    fun forCategory(category: String): ImageVector =
        when (category) {
            "VIDEO" -> Video
            "PHOTO" -> Photo
            "AUDIO" -> Audio
            "ARCHIVE" -> ArchiveFile
            "APP" -> AppPackage
            else -> GenericFile
        }

    /** File-extension icon for on-device file browsing (SelectFilesScreen, File Explorer, etc.). */
    fun forFileName(name: String): ImageVector =
        when {
            name.endsWith(".pdf", true) -> PdfFile
            name.endsWith(".zip", true) || name.endsWith(".rar", true) || name.endsWith(".7z", true) -> ArchiveFile
            name.endsWith(".doc", true) || name.endsWith(".docx", true) ||
                name.endsWith(".xls", true) || name.endsWith(".xlsx", true) ||
                name.endsWith(".ppt", true) || name.endsWith(".pptx", true) -> DocFile
            name.endsWith(".apk", true) -> AppPackage
            name.endsWith(".txt", true) || name.endsWith(".md", true) || name.endsWith(".log", true) ||
                name.endsWith(".json", true) || name.endsWith(".xml", true) -> TextFile
            name.endsWith(".jpg", true) || name.endsWith(".jpeg", true) || name.endsWith(".png", true) ||
                name.endsWith(".gif", true) || name.endsWith(".webp", true) || name.endsWith(".heic", true) -> Photo
            name.endsWith(".mp4", true) || name.endsWith(".mkv", true) || name.endsWith(".webm", true) ||
                name.endsWith(".mov", true) || name.endsWith(".avi", true) -> Video
            name.endsWith(".mp3", true) || name.endsWith(".m4a", true) || name.endsWith(".wav", true) ||
                name.endsWith(".flac", true) || name.endsWith(".ogg", true) -> Audio
            else -> GenericFile
        }

    /** Category icon for the on-device file browser tabs (Photos/Videos/Audio/Documents/Apps). */
    fun forBrowseCategory(category: String): ImageVector =
        when (category) {
            "Photos" -> Photo
            "Videos" -> Video
            "Audio" -> Audio
            "Apps" -> AppPackage
            else -> GenericFile
        }
}
