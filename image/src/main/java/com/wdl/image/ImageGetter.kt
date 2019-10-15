package com.wdl.image

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.core.content.FileProvider
import java.io.File
import java.lang.Exception
import java.lang.RuntimeException
import java.lang.ref.WeakReference

/**
 * Create by: wdl at 2019/10/15 10:11
 */
@Suppress("unused")
class ImageGetter(activity: Activity) {
    /**
     * 虚引用，防止内存泄漏
     */
    private val weakReference: WeakReference<Activity> = WeakReference(activity)

    /**
     * 回调码
     */
    private val REQUEST_CODE_GET_IMAGE_FROM_CAMERA = 0X01
    private val REQUEST_CODE_GET_IMAGE_FROM_ALBUM = REQUEST_CODE_GET_IMAGE_FROM_CAMERA + 1

    /**
     * 路径
     */
    private var mCameraImageDir: File? = null
    private var mCameraImageFile: File? = null


    var mCallback: Callback? = null
        set(value) {
            field = value
        }

    interface Callback {
        /**
         * 相机拍照回调
         */
        fun onResultFromCamera(file: File)

        /**
         * 相册选取回调
         */
        fun onResultFromAlbum(file: File)

        /**
         * 错误信息回调
         */
        fun onError(errorMsg: String)
    }

    /**
     * 获取Ac
     */
    private fun getActivity(): Activity {
        return weakReference.get()!!
    }


    /**
     * 系统相册获取图片
     */
    public fun getImageFromAlbum() {
        try {
            val intent = Intent()
            intent.action = Intent.ACTION_PICK
            intent.data = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            getActivity().startActivityForResult(intent, REQUEST_CODE_GET_IMAGE_FROM_ALBUM)
        } catch (e: Exception) {
            e.printStackTrace()
            mCallback?.onError(e.toString())
        }
    }

    /**
     * Camera获取图片
     */
    public fun getImageFromCamera() {
        if (getCameraImageDir() == null) {
            mCallback?.onError("获取缓存目录失败")
            return
        }
        try {
            mCameraImageFile = newFileUnderDir(getCameraImageDir(), ".jpg")
            val intent = Intent()
            intent.action = MediaStore.ACTION_IMAGE_CAPTURE
            val uri: Uri?
            try {
                uri = FileProvider.getUriForFile(
                    getActivity(),
                    getActivity().packageName + ".FileProvider", mCameraImageFile!!
                )
            } catch (e: Exception) {
                e.printStackTrace()
                throw RuntimeException("请检查AndroidManifest.xml文件是否添加FileProvider")
            }
            intent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
            getActivity().startActivityForResult(intent, REQUEST_CODE_GET_IMAGE_FROM_CAMERA)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 文件保存位置
     */
    private fun newFileUnderDir(dir: File?, ext: String): File? {
        if (dir == null) return null
        var currentTime = System.currentTimeMillis()
        var file = File(dir, "${currentTime}$ext")
        if (!file.exists()) {
            file.mkdir()
        }
        while (file.exists()) {
            file = File(dir, "${currentTime++}$ext")
        }
        return file
    }

    /**
     *  获取Camera缓存目录
     */
    private fun getCameraImageDir(): File? {
        if (mCameraImageDir == null) {
            var dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            if (dir == null) {
                dir = getActivity().cacheDir
            }
            mCameraImageDir = dir
        }

        if (!mCameraImageDir!!.exists()) {
            mCameraImageDir!!.mkdirs()
        }

        return mCameraImageDir
    }

    /**
     * 通知更新图库
     */
    private fun scanFile(context: Context, file: File?) {

        if (file == null || !file.exists()) {
            Log.e("scanFile", "scanFile : ------------------------------")
            return
        }
        val intent = Intent()
        intent.action = Intent.ACTION_MEDIA_SCANNER_SCAN_FILE
        intent.data = Uri.fromFile(file)
        context.sendBroadcast(intent)
    }

    /**
     * 获取image路径
     */
    private fun getDataColumn(
        context: Context,
        uri: Uri,
        selection: String?,
        selectionArgs: Array<String>?
    ): String? {
        var cursor: Cursor? = null
        val column = MediaStore.Images.Media.DATA
        val projection = arrayOf(column)
        try {
            cursor = context.contentResolver.query(uri, projection, selection, selectionArgs, null)
            if (cursor!!.moveToFirst()) {
                val columnIndex = cursor.getColumnIndexOrThrow(column)
                return cursor.getString(columnIndex)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            cursor?.close()
        }
        return null
    }

    /**
     * 回调转接
     */
    public fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (mCallback == null) return

        when (requestCode) {
            REQUEST_CODE_GET_IMAGE_FROM_CAMERA -> {
                if (resultCode == Activity.RESULT_OK) {
                    scanFile(getActivity(), mCameraImageFile)
                    mCallback!!.onResultFromCamera(mCameraImageFile!!)
                }
            }

            REQUEST_CODE_GET_IMAGE_FROM_ALBUM -> {
                if (resultCode == Activity.RESULT_OK) {
                    if (data == null) {
                        mCallback!!.onError("从相册获取图片失败(intent为空)")
                        return
                    }
                    val uri = data.data
                    if (uri == null) {
                        mCallback!!.onError("从相册获取图片失败(intent为空)")
                        return
                    }

                    val path: String?
                    try {
                        path = getDataColumn(getActivity(), uri, null, null)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        mCallback!!.onError("从相册获取图片失败")
                        return
                    }
                    if (path.isNullOrEmpty()) {
                        mCallback!!.onError("从相册获取图片失败(路径为空)")
                        return
                    }

                    mCallback!!.onResultFromAlbum(File(path))
                }
            }
        }
    }


}