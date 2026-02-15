package com.scandock.app.provider

import android.content.ContentProvider
import android.content.ContentValues
import android.net.Uri
import android.os.ParcelFileDescriptor
import androidx.core.content.FileProvider
import java.io.File

class ScanDockProvider : FileProvider() {}

//{
//    companion object {
//        private const val AUTHORITY = "com.scandock.app.provider"
//
//        fun getUriForFile(file: File): Uri {
//            return Uri.parse("content://$AUTHORITY/scans/${file.name}")
//        }
//    }
//
//    override fun onCreate(): Boolean = true
//
//    override fun openFile(uri: Uri, mode: String): ParcelFileDescriptor? {
//        val fileName = uri.lastPathSegment ?: return null
//        val file = File(context!!.filesDir, fileName)
//        return ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
//    }
//
//    override fun query(
//        uri: Uri, projection: Array<out String>?,
//        selection: String?, selectionArgs: Array<out String>?,
//        sortOrder: String?
//    ) = null
//
//    override fun getType(uri: Uri): String = "image/jpeg"
//    override fun insert(uri: Uri, values: ContentValues?) = null
//    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?) = 0
//    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?) = 0
//}