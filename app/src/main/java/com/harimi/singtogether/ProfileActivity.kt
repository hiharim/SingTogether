package com.harimi.singtogether

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.core.net.toFile
import com.bumptech.glide.Glide
import com.google.android.material.textfield.TextInputEditText
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import com.harimi.singtogether.Network.RetrofitClient
import com.harimi.singtogether.Network.RetrofitService
import com.harimi.singtogether.databinding.ActivityProfileBinding
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

// 회원가입할때 프로필,닉네임 설정하는 액티비티
class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private lateinit var retrofit : Retrofit
    private lateinit var retrofitService: RetrofitService


    private lateinit var nickname : String
    private lateinit var email : String
    private lateinit var profile : String
    private lateinit var social : String
    private lateinit var token : String

    private var mediaPath: String? = null
    private var imageFile : File?=null
    val REQUEST_IMAGE_CAPTURE = 1 // 카메라로 사진 찍기 상수
    val REQUEST_GALLERY_TAKE = 2 // 갤러리에서 사진 선택 상수
    lateinit var currentPhotoPath : String
    lateinit var fileName : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val context : Context =this
        // 서버 연결
        initRetrofit()
        textWatcher()

        // 인텐트 값 받기
        email= intent.getStringExtra("EMAIL").toString()
        nickname= intent.getStringExtra("NICKNAME").toString()
        profile=intent.getStringExtra("PROFILE").toString()
        social=intent.getStringExtra("SOCIAL").toString()
        token="a"

        Log.e("값: ", email + " " + nickname + " " + social + " " + token + " " + profile)
        Log.e("imageFile값: ", imageFile.toString())
        // 닉네임받아와서 set해줌
        binding.activityProfileEt.setText(nickname)
        // 프로필받아와서 set해줌
        Glide.with(context).load(profile).into(binding.activityProfileIv)

        // 프로필사진 클릭
        binding.activityProfileIv.setOnClickListener {

        }

        //이미지 변경 클릭
        binding.activityProfileBtnPhoto.setOnClickListener {
            settingPermission()
        }

        // 완료 버튼 클릭 - 회원가입완료
        binding.activityProfileBtn.setOnClickListener {
            Log.e("서버에 보내는값: ", email + " " + nickname + " " + social + " " + token)
            if(imageFile == null) {
                // imageFile 이 null 이면 join_none.php - 프로필사진 변경X 기존 소셜 프로필 사용 O
                if (email != null && nickname !=null && social !=null && token != null && profile !=null) {
                    retrofitService.requestJoinNone(email, nickname, social, token, profile)
                            .enqueue(object : Callback<String> {
                                override fun onResponse(
                                        call: Call<String>,
                                        response: Response<String>
                                ) {
                                    if (response.isSuccessful) {
                                        Log.d("onResponse: 성공: ", response.body() + response.message())
                                        user_info.user_email =email
                                        user_info.user_profile =profile
                                        val intent = Intent(context, MainActivity::class.java)
                                        startActivity(intent)
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
            }else{
                // 프로필 사진 변경했을때
                Log.e("imageFile 값: ", imageFile.toString())
                Log.e("fileName 값: ", fileName)
                var requestBody : RequestBody= RequestBody.create(MediaType.parse("multipart/form-data"), imageFile)
                var body : MultipartBody.Part=MultipartBody.Part.createFormData("uploaded_file", fileName, requestBody)
                if (email != null && nickname !=null && social !=null && token !=null) {
                    retrofitService.requestJoin(email, nickname, social, token, body)
                            .enqueue(object : Callback<String> {
                                override fun onResponse(
                                        call: Call<String>,
                                        response: Response<String>
                                ) {
                                    if (response.isSuccessful) {
                                        Log.d("onResponse: 성공: ", response.body() + response.message())

                                        val jsonObject = JSONObject(response.body().toString())
                                        val profile_image = jsonObject.getString("profile")
                                        Log.d("get_profile_image: ", profile_image)

                                        user_info.user_email =email
                                        user_info.user_profile =profile_image
                                        val intent = Intent(context, MainActivity::class.java)
                                        startActivity(intent)
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
    }

    // edittext 힌트 밑에 보여주는 함수
    private fun textWatcher(){
        binding.activityProfileEt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                if(binding.activityProfileEt.text!!.isEmpty()){
                    binding.TextInputLayout.error="닉네임을 입력해주세요"
                }else{
                    binding.TextInputLayout.error=null
                }
            }

        })
    }

    // 테드퍼미션
    fun settingPermission() {
        var permis = object  : PermissionListener {
            // 어떠한 형식을 상속받는 익명 클래스의 객체를 생성하기 위해 다음과 같이 작성
            override fun onPermissionGranted() {
                val builder = AlertDialog.Builder(this@ProfileActivity)
                builder.setTitle("업로드할 이미지 선택")
                builder.setPositiveButton("카메라로 사진찍기") { dialogInterface, i -> startCapture() }
                builder.setNeutralButton("앨범에서 사진선택") { dialogInterface, i -> openGalleryForImage() }
                builder.show()
            }

            override fun onPermissionDenied(deniedPermissions: MutableList<String>?) {
                Toast.makeText(this@ProfileActivity, "권한 거부", Toast.LENGTH_SHORT)
                        .show()
                ActivityCompat.finishAffinity(this@ProfileActivity) // 권한 거부시 앱 종료
            }
        }

        TedPermission.with(this)
                .setPermissionListener(permis)
                .setRationaleMessage("카메라 사진 권한 필요")
                .setDeniedMessage("카메라 권한 요청 거부")
                .setPermissions(
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
//                android.Manifest.permission.READ_EXTERNAL_STORAGE,
                        android.Manifest.permission.CAMERA)
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
    private fun createImageFile() : File {
        val timeStamp : String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir : File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
                "JPEG_${timeStamp}_",
                ".jpg",
                storageDir
        ).apply{
            currentPhotoPath = absolutePath
        }
    }

    //갤러리에서 사진 선택하기
    // todo : 갤러리에서 선택한 사진을 file 형태로 만들어야함..
    private fun openGalleryForImage() {

        val intent=Intent(Intent.ACTION_PICK)
        intent.type=MediaStore.Images.Media.CONTENT_TYPE
        //createImageFile()
        startActivityForResult(intent, REQUEST_GALLERY_TAKE)
    }

    //
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val timeStamp : String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        if(requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK){
            val file = File(currentPhotoPath)
            imageFile=file
            // Create an image file name
            fileName = "JPEG_$timeStamp.jpg"

            if (Build.VERSION.SDK_INT < 28) {
                val bitmap = MediaStore.Images.Media
                        .getBitmap(contentResolver, Uri.fromFile(file))
                binding.activityProfileIv.setImageBitmap(bitmap)
            }
            else{
                val decode = ImageDecoder.createSource(this.contentResolver,
                        Uri.fromFile(file))
                val bitmap = ImageDecoder.decodeBitmap(decode)
                binding.activityProfileIv.setImageBitmap(bitmap)
            }
        }else if(requestCode == REQUEST_GALLERY_TAKE && resultCode == Activity.RESULT_OK){
            data?.data?.let{ uri ->
                val selectedImage = uri
                val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)

                val cursor = contentResolver.query(selectedImage!!, filePathColumn, null, null, null)
                assert(cursor != null)
                cursor!!.moveToFirst()

                val columnIndex = cursor.getColumnIndex(filePathColumn[0])
                mediaPath = cursor.getString(columnIndex)
                binding.activityProfileIv.setImageURI(uri)

                cursor.close()

                val file = File(mediaPath)
                imageFile = file
                fileName = "JPEG_$timeStamp.jpg"
                Log.e("갤러리 imageFile: ", imageFile.toString())
//                val file = File(currentPhotoPath)
//                imageFile=file
////                imageFile = File(uri.toString())
////                //imageFile = File(uri.getPath())
//                fileName = "JPEG_$timeStamp.jpg"
//                Log.e("갤러리 imageFile: ", imageFile.toString())
                binding.activityProfileIv.setImageURI(uri)
            }
            //binding.activityProfileIv.setImageURI(data?.data)
        }
    }




    private fun initRetrofit(){
        retrofit=RetrofitClient.getInstance()
        retrofitService=retrofit.create(RetrofitService::class.java)
    }


    //스태틱을 쓰기위한 함수
    class user_info{
        companion object {
            var user_email = ""
            var user_profile = ""


        }
    }
}