package com.wdl.image

import java.io.*
import java.lang.Exception
import java.net.URL

/**
 * Create by: wdl at 2019/10/15 14:28
 * 图片下载
 */
@Suppress("unused")
object ImageDownload {

    /**
     * 下载图片
     * @param remotePath 远程URL
     * @param savePath 保存路径
     */
    fun download(
        remotePath: String,
        savePath: String? = null,
        downloadListener: DownloadListener
    ) {
        var input: InputStream? = null
        var outputStream: OutputStream? = null
        try {
            val imageString = remotePath.substring(remotePath.lastIndexOf("/") + 1)
            val path = "/storage/emulated/0/download/$imageString"

            val file = File(path)
            if (!file.parentFile!!.exists()) {
                file.parentFile!!.mkdirs()
            }
            if (file.exists() && file.length() > 0) {
                downloadListener.onComplete(path)
                return
            }

            val url = URL(remotePath)
            val urlConnection = url.openConnection()
            urlConnection.connect()
            val totalLength = urlConnection.contentLength
            val inputStream = urlConnection.getInputStream()
            input = BufferedInputStream(inputStream, 8 * 1024)

            outputStream = FileOutputStream(path)
            var downloadSize = 0
            var count: Int

            input.readBytes().let {
                count = it.size
                outputStream.write(it, 0, count)
                downloadSize += count
                downloadListener.onProgress((downloadSize.toFloat() / totalLength.toFloat() * 100.0F).toInt())
                it
            }
            downloadListener.onComplete(path)

        } catch (e: Exception) {
            e.printStackTrace()
            downloadListener.onError(e)
        } finally {
            outputStream?.flush()
            outputStream?.close()
            input?.close()
        }
    }

    interface DownloadListener {
        /**
         * 下载成功
         * @param savePath 保存的路径
         */
        fun onComplete(savePath: String)

        /**
         * 下载进度
         * @param progress 进度
         */
        fun onProgress(progress: Int)

        /**
         * 下载失败
         * @param e Exception
         */
        fun onError(e: Exception)
    }
}