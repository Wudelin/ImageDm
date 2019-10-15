# ImageDm
封装相机、相册获取图片


How to use?
--------

	class MainActivity : AppCompatActivity() {
    private var imageGetter: ImageGetter? = null
    
    // Step 1
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

         // Step 2
        mCamera.setOnClickListener {
            imageGetter?.getImageFromCamera()
        }

        mAlbum.setOnClickListener {
            imageGetter?.getImageFromAlbum()
        }

    }

     // Step 3
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // 转交
        getImageGetter()!!.onActivityResult(requestCode, resultCode, data)
    }
   }

Attention : 1.Add provider into your AndroidManifest.xml 
-------------
2.res/xml/rc_file_path -- already in image Module 
-------------
3.Permission can use https://github.com/Wudelin/Permission
-------------

        <provider
            android:name="androidx.core.content.FileProvider"
            android:authorities="${applicationId}.FileProvider"
            android:exported="false"
            android:grantUriPermissions="true">
            <meta-data
                android:name="android.support.FILE_PROVIDER_PATHS"
                android:resource="@xml/rc_file_path" />
        </provider>
        
        






Download
--------

```
allprojects {
	repositories {
		...
		maven { url 'https://jitpack.io' }
	}
}
  
dependencies {
	implementation 'com.github.Wudelin:ImageDm:1.0.0'
}
```


