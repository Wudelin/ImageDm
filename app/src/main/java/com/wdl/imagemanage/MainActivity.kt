package com.wdl.imagemanage

import android.Manifest
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.wdl.image.ImageDownload
import com.wdl.image.ImageGetter
import com.wdl.lib.PermissionUtil
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.lang.Exception

class MainActivity : AppCompatActivity() {
    private var imageGetter: ImageGetter? = null
    private fun getImageGetter(): ImageGetter? {
        if (imageGetter == null) {
            imageGetter = ImageGetter(this)
        }
        imageGetter!!.mCallback = object : ImageGetter.Callback {
            override fun onResultFromCamera(file: File) {
                Log.e("MainActivity", "onResultFromCamera ${file.absolutePath}")
            }

            override fun onResultFromAlbum(file: File) {
                Log.e("MainActivity", "onResultFromAlbum ${file.absolutePath}")
            }

            override fun onError(errorMsg: String) {
                Log.e("MainActivity", errorMsg)
            }

        }
        return imageGetter
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        imageGetter = getImageGetter()
        PermissionUtil.init(this).requestPermission(arrayOf(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE
        ),
            object : PermissionUtil.IPermissionCallback {
                override fun succeed(permissions: MutableList<String>?) {
                }

                override fun failure(permissions: MutableList<String>?) {
                }

                override fun unRequiredApply() {
                }

                override fun succeed() {

                }

                override fun failure() {
                    Toast.makeText(this@MainActivity, "权限未获取", Toast.LENGTH_SHORT).show()
                }

            })

        mCamera.setOnClickListener {
            imageGetter?.getImageFromCamera()
        }

        mAlbum.setOnClickListener {
            imageGetter?.getImageFromAlbum()
        }

        mDownload.setOnClickListener {
            val remotePath = "http://resource.yy.fanwe.cn/vcoin/emoji/256_20191015151259045079.jpg"
            Thread(Runnable {
                ImageDownload.download(remotePath, null, object : ImageDownload.DownloadListener {
                    override fun onProgress(progress: Int) {
                        Log.e("MainActivity", "onProgress : $progress")
                    }

                    override fun onError(e: Exception) {
                        Log.e("MainActivity", "onError : ${e.message}")
                    }

                    override fun onComplete(savePath: String) {
                        Log.e("MainActivity", "onComplete : $savePath")
                    }

                })
            }).start()

        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // 转交
        getImageGetter()!!.onActivityResult(requestCode, resultCode, data)
    }
}
