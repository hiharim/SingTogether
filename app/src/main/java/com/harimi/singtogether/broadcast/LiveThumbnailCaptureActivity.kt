package com.harimi.singtogether.broadcast

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import com.harimi.singtogether.LoginActivity
import com.harimi.singtogether.Network.RetrofitClient
import com.harimi.singtogether.Network.RetrofitService

import com.harimi.singtogether.databinding.ActivityLiveThumbnailCaptureBinding
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class LiveThumbnailCaptureActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLiveThumbnailCaptureBinding
    private var mediaPath: String? = null
    private var imageFile: File? = null
    val REQUEST_IMAGE_CAPTURE = 1 // 카메라로 사진 찍기 상수
    val REQUEST_GALLERY_TAKE = 2 // 갤러리에서 사진 선택 상수
    private var currentPhotoPath: String ? =null
    private var fileName: String ? =null
    private lateinit var retrofit: Retrofit
    private lateinit var retrofitService: RetrofitService
    private var liveTitle: String? = null
    private var email: String? = null
    private var profile: String? = null
    private var nickname: String? = null
    private var TAG  = "LIVETHUMBNAILCAPTUREACTIVITY"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLiveThumbnailCaptureBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val getIntent = intent
        liveTitle = getIntent.getStringExtra("liveTitle")

        email = LoginActivity.user_info.loginUserEmail
        profile = LoginActivity.user_info.loginUserProfile
        nickname = LoginActivity.user_info.loginUserNickname

        binding.ivThumbnail.visibility =View.INVISIBLE

        Log.d(TAG,"아이디 "+email +" 닉네임 "+nickname+" 프로필사진 "+profile)
        settingPermission()

        binding.ivCancelThumbnailCapture.setOnClickListener {

            val builder = AlertDialog.Builder(this)
            builder.setTitle("취소")
            builder.setMessage("방 만들기를 취소하겠습니까?")

            builder.setPositiveButton("네") { dialog, which ->
                Toast.makeText(applicationContext,
                    "방만들기를 취소하였습니다", Toast.LENGTH_SHORT).show()

                finish()
            }
            builder.setNegativeButton("아니요") { dialog, which ->
                if (imageFile == null){
                    settingPermission()
                }else{

                }
            }

            builder.show()
        }

        binding.btnLiveStart.setOnClickListener {

            if (imageFile ==null){
                Toast.makeText(applicationContext,"썸네일을 먼저 등록해주세요.",Toast.LENGTH_SHORT).show()
                settingPermission()
                return@setOnClickListener
                binding.ivThumbnail.visibility =View.INVISIBLE
            }

            binding.ivThumbnail.visibility =View.VISIBLE
            retrofit = RetrofitClient.getInstance()
            retrofitService = retrofit.create(RetrofitService::class.java)
            Log.e("imageFile 값: ", imageFile.toString())
            Log.e("fileName 값: ", fileName!!)
            var requestBody: RequestBody =
                RequestBody.create(MediaType.parse("multipart/form-data"), imageFile)
            var body: MultipartBody.Part =
                MultipartBody.Part.createFormData("uploaded_file", fileName, requestBody)
            if (email != null && nickname != null &&profile != null &&liveTitle  !=null ) {
                retrofitService.requestLiveStreamingStart(email!!,nickname!!,profile!!,liveTitle!!, body)
                    .enqueue(object : Callback<String> {
                        override fun onResponse(
                            call: Call<String>,
                            response: Response<String>
                        ) {
                            if (response.isSuccessful) {

                                val jsonObject = JSONObject(response.body().toString())
                                val roomIdx = jsonObject.getString("roomIdx")

                                Log.d("onResponse: 성공: ", response.body() + response.message())
                                val intent = Intent(this@LiveThumbnailCaptureActivity, LiveStreamingActivity::class.java)
                                intent.putExtra("roomIdx", roomIdx)
                                startActivity(intent)
                                finish()
                            } else {
                                Log.e("onResponse", "실패 : " + response.errorBody())
                            }
                        }

                        override fun onFailure(call: Call<String>, t: Throwable) {
                            Log.d(
                                "실패:", "Failed API call with call: " + call +
                                        " + exception: " + t
                            )
                        }

                    })
            }
        }

    }

    fun settingPermission() {
        var permis = object : PermissionListener {
            // 어떠한 형식을 상속받는 익명 클래스의 객체를 생성하기 위해 다음과 같이 작성
            override fun onPermissionGranted() {
                val builder = AlertDialog.Builder(this@LiveThumbnailCaptureActivity)
                builder.setTitle("썸네일을 등록해주세요")
                builder.setPositiveButton("카메라로 사진찍기") { dialogInterface, i -> startCapture() }
                builder.setNeutralButton("앨범에서 사진선택") { dialogInterface, i -> openGalleryForImage() }
                builder.show()
            }

            override fun onPermissionDenied(deniedPermissions: MutableList<String>?) {
                Toast.makeText(this@LiveThumbnailCaptureActivity, "권한 거부", Toast.LENGTH_SHORT)
                    .show()
                ActivityCompat.finishAffinity(this@LiveThumbnailCaptureActivity) // 권한 거부시 앱 종료
            }
        }

        TedPermission.with(this)
            .setPermissionListener(permis)
            .setRationaleMessage("카메라 사진 권한 필요")
            .setDeniedMessage("카메라 권한 요청 거부")
            .setPermissions(
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
//                android.Manifest.permission.READ_EXTERNAL_STORAGE,
                android.Manifest.permission.CAMERA
            )
            .check()
    }

    // 카메라로 사진 찍기
    fun startCapture() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(packageManager)?.also {
                val photoFile: File? = try {
                    createImageFile()
                } catch (ex: IOException) {
                    null
                }
                photoFile?.also {
                    val photoURI: Uri = FileProvider.getUriForFile(
                        this,
                        "com.harimi.singtogether.fileprovider",
                        it
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
                }
            }
        }
    }

    // 카메라로 사진 찍은 후 파일만들기
    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        ).apply {
            currentPhotoPath = absolutePath
        }
    }

    //갤러리에서 사진 선택하기
    private fun openGalleryForImage() {

        val intent = Intent(Intent.ACTION_PICK)
        intent.type = MediaStore.Images.Media.CONTENT_TYPE
        //createImageFile()
        startActivityForResult(intent, REQUEST_GALLERY_TAKE)
    }

    //
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        if (requestCode == REQUEST_IMAGE_CAPTURE) {
            if (resultCode == Activity.RESULT_OK) {
                binding.ivThumbnail.visibility =View.VISIBLE
                val file = File(currentPhotoPath)
                imageFile = file
                // Create an image file name
                fileName = "JPEG_$timeStamp.jpg"

                if (Build.VERSION.SDK_INT < 28) {
                    val bitmap = MediaStore.Images.Media
                        .getBitmap(contentResolver, Uri.fromFile(file))
                    binding.ivThumbnail.setImageBitmap(bitmap)
                } else {
                    val decode = ImageDecoder.createSource(
                        this.contentResolver,
                        Uri.fromFile(file)
                    )
                    val bitmap = ImageDecoder.decodeBitmap(decode)
                    binding.ivThumbnail.setImageBitmap(bitmap)
                }
            }else{
                binding.ivThumbnail.visibility =View.INVISIBLE
                Toast.makeText(applicationContext,"썸네일을 다시 등록해주세요.",Toast.LENGTH_SHORT).show()
                settingPermission()
                return
            }
        } else if (requestCode == REQUEST_GALLERY_TAKE) {
            if (resultCode == Activity.RESULT_OK) {
                binding.ivThumbnail.visibility =View.VISIBLE
                    data?.data?.let { uri ->
                        val selectedImage = uri
                        val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)

                        val cursor =
                            contentResolver.query(selectedImage!!, filePathColumn, null, null, null)
                        assert(cursor != null)
                        cursor!!.moveToFirst()

                        val columnIndex = cursor.getColumnIndex(filePathColumn[0])
                        mediaPath = cursor.getString(columnIndex)
                        binding.ivThumbnail.setImageURI(uri)
                        cursor.close()

                        val file = File(mediaPath)
                        imageFile = file
                        fileName = "JPEG_$timeStamp.jpg"
                        Log.e("갤러리 imageFile: ", imageFile.toString())

                        binding.ivThumbnail.setImageURI(uri)
                    }
                }else{
                    binding.ivThumbnail.visibility =View.INVISIBLE
                    Toast.makeText(applicationContext,"썸네일을 다시 등록해주세요.",Toast.LENGTH_SHORT).show()
                    settingPermission()
                    return
                }
            }
    }

}