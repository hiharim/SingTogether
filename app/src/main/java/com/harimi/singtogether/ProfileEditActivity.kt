package com.harimi.singtogether

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.InputType
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.bumptech.glide.Glide
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import com.harimi.singtogether.Network.RetrofitClient
import com.harimi.singtogether.Network.RetrofitService
import de.hdodenhof.circleimageview.CircleImageView
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


class ProfileEditActivity : AppCompatActivity() {
    private var TAG :String = "ProfileEdit_Activity"

    private lateinit var iv_profileImage : CircleImageView
    private lateinit var tv_nickName : TextView
    private lateinit var btn_cancel : Button
    private lateinit var btn_profileEdit : Button
    private lateinit var iv_reCreateNickname : ImageView


//    private lateinit var btn_reCreateNickname : Button
    private lateinit var tv_needCheck : TextView
    private lateinit var tv_checkFinish : TextView


    private var mediaPath: String? = null
    private var imageFile : File?=null
    val REQUEST_IMAGE_CAPTURE = 1 // 카메라로 사진 찍기 상수
    val REQUEST_GALLERY_TAKE = 2 // 갤러리에서 사진 선택 상수
    lateinit var currentPhotoPath : String
    lateinit var fileName : String
    private lateinit var retrofit : Retrofit
    private lateinit var retrofitService: RetrofitService

    private var email : String ? = null
    private var profile : String ? = null
    private var nickname : String ? = null

    private var isProfileNicknameEdit : Boolean ? = false
    private var isProfileImageEdit : Boolean ? = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_edit)
        tv_nickName = findViewById(R.id.tv_nickName)
        iv_profileImage = findViewById(R.id.iv_profileImage)
        btn_cancel = findViewById(R.id.btn_cancel)
        btn_profileEdit = findViewById(R.id.btn_profileEdit)
        iv_reCreateNickname = findViewById(R.id.iv_reCreateNickname)

//        btn_reCreateNickname = findViewById(R.id.btn_reCreateNickname)
        tv_needCheck = findViewById(R.id.tv_needCheck)
        tv_checkFinish = findViewById(R.id.tv_checkFinish)



        nickname=LoginActivity.user_info.loginUserNickname
        profile =LoginActivity.user_info.loginUserProfile
        email =LoginActivity.user_info.loginUserEmail

        initRetrofit()

        tv_nickName.setText(nickname)


        tv_checkFinish.visibility =View.GONE
        tv_needCheck.visibility =View.GONE


        isProfileNicknameEdit =true

        iv_reCreateNickname.setOnClickListener {


//            if (tv_nickName.text.toString().equals( "")){
//                Toast.makeText(this ,"닉네임을 입력해주세요",Toast.LENGTH_SHORT).show()
//                return@setOnClickListener
//            }

            val builder = AlertDialog.Builder(this@ProfileEditActivity)
            builder
                .setMessage("닉네임 변경")
                .setCancelable(false)

            val editUserName = EditText(this)
            editUserName.setSingleLine()
            editUserName.setText(tv_nickName.text.toString())

            ////editText 마진 값 주기
            val container = FrameLayout(this@ProfileEditActivity)
            val params = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            params.leftMargin = 50
            params.rightMargin = 50
            editUserName.setLayoutParams(params)
            container.addView(editUserName)
            builder.setView(container)
            builder.setPositiveButton("변경",
                DialogInterface.OnClickListener { dialog, which ->
                    tv_nickName.setText(editUserName.text.toString())

                    if (LoginActivity.user_info.loginUserNickname.equals(editUserName.text.toString())){
                        tv_needCheck.visibility = View.GONE
                        tv_checkFinish.visibility = View.GONE
                        isProfileNicknameEdit =true
                        return@OnClickListener

                    }

                    retrofitService.requestNicknameCheck(tv_nickName.text.toString())
                        .enqueue(object : Callback<String> {
                            override fun onResponse(
                                call: Call<String>,
                                response: Response<String>
                            ) {
                                if (response.isSuccessful) {

                                    var jsonObject = JSONObject(response.body().toString())
                                    var result = jsonObject.getBoolean("result")

                                    if (result){
                                        tv_needCheck.visibility = View.GONE
                                        tv_checkFinish.visibility = View.VISIBLE
                                        isProfileNicknameEdit =true


                                    }else{
                                        tv_needCheck.visibility = View.VISIBLE
                                        tv_checkFinish.visibility = View.GONE
                                        isProfileNicknameEdit =false
//                                        tv_nickName.setText("")
                                        Toast.makeText(this@ProfileEditActivity ,"닉네임이 중복됩니다. 다시 설정해주세요.",Toast.LENGTH_SHORT).show()
                                    }


                                } else {

                                }
                            }

                            override fun onFailure(call: Call<String>, t: Throwable) {
                                Log.d(
                                    "실패:", "Failed API call with call: " + call +
                                            " + exception: " + t
                                )
                            }

                        })

                })
            // 취소 버튼 설정
            builder.setNegativeButton("취소",
                DialogInterface.OnClickListener { dialog, which -> dialog.dismiss() })
            builder.show()


        }

        if (profile.equals("null")){
            iv_profileImage.setImageResource(R.mipmap.ic_launcher_round)
        }else{
            Glide.with(this).load("http://3.35.236.251/" + profile).into(iv_profileImage)

        }

        ////프로필 변경
        iv_profileImage.setOnClickListener {
            settingPermission()
        }
        ///////////////
        /////닉네임 변경
//        tv_nickName.setOnClickListener {
//            editNickName()
//        }
//        iv_nickNameEdit.setOnClickListener {
//            editNickName()
//        }
        //////////////




        btn_profileEdit.setOnClickListener {
            editProfile()
        }

        btn_cancel.setOnClickListener {
            finishActivity()
        }

    }

    private fun finishActivity(){
            val builder: AlertDialog.Builder = AlertDialog.Builder(this)
            builder
                .setTitle("나가시겠습니까?")
                .setCancelable(false)
            builder.setPositiveButton("네", DialogInterface.OnClickListener { dialog, which ->
                finish()
            })
            builder.setNegativeButton("아니요", DialogInterface.OnClickListener { dialog, which -> dialog.cancel()

            })
            builder.show()
    }

    private fun editNickName(){
        val builder = AlertDialog.Builder(this@ProfileEditActivity)
        builder
            .setMessage("닉네임 변경")
            .setCancelable(false)

        val editUserName = EditText(this)
        editUserName.setSingleLine()
        editUserName.setText(tv_nickName.text.toString())

        ////editText 마진 값 주기
        val container = FrameLayout(this@ProfileEditActivity)
        val params = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        params.leftMargin = 50
        params.rightMargin = 50
        editUserName.setLayoutParams(params)
        container.addView(editUserName)
        builder.setView(container)
        builder.setPositiveButton("변경",
            DialogInterface.OnClickListener { dialog, which ->
                tv_nickName.setText(editUserName.text.toString())
            })
        // 취소 버튼 설정
        builder.setNegativeButton("취소",
            DialogInterface.OnClickListener { dialog, which -> dialog.dismiss() })
        builder.show()
//            val builder: AlertDialog.Builder = AlertDialog.Builder(this)
//            builder
//                .setTitle("닉네임 수정")
//                .setCancelable(false)
//
//            val input = EditText(this)
//            input.setSingleLine()
//            input.setText(tv_nickName.text.toString())
//            input.inputType = InputType.TYPE_CLASS_TEXT
//            builder.setView(input)
//            builder.setPositiveButton("확인", DialogInterface.OnClickListener { dialog, which ->
//                tv_nickName.setText(input.text.toString())
//
//            })
//            builder.setNegativeButton(
//                "취소",
//                DialogInterface.OnClickListener { dialog, which -> dialog.cancel() })
//            builder.show()


    }
    private fun editProfile(){


        if (isProfileNicknameEdit == false){
//            Toast.makeText(this,"닉네임 중복확인을 해주세요." ,Toast.LENGTH_SHORT).show()

            val builder: AlertDialog.Builder = AlertDialog.Builder(this)
            builder
                .setTitle("닉네임 중복확인을 해주세요")
                .setCancelable(false)
            builder.setPositiveButton("네", DialogInterface.OnClickListener { dialog, which ->

            })
            builder.show()
            return
        }
//        if(isProfileImageEdit ==false && LoginActivity.user_info.loginUserNickname.equals(tv_nickName.text.toString())){
//            val builder: AlertDialog.Builder = AlertDialog.Builder(this)
//            builder
//                .setTitle("수정 된 것이 없습니다")
//                .setCancelable(false)
//            builder.setPositiveButton("네", DialogInterface.OnClickListener { dialog, which ->
//                return@OnClickListener
//            })
//            builder.show()
//        }else{
            if (isProfileImageEdit!!){
                var updateNickname = tv_nickName.text.toString()
                var requestBody : RequestBody = RequestBody.create(
                    MediaType.parse("multipart/form-data"),
                    imageFile
                )
                var body : MultipartBody.Part=
                    MultipartBody.Part.createFormData("uploaded_file", fileName, requestBody)
                retrofitService.requestEditUserProfile(email!!, updateNickname, body)
                    .enqueue(object : Callback<String> {
                        override fun onResponse(
                            call: Call<String>,
                            response: Response<String>
                        ) {
                            if (response.isSuccessful) {
                                Log.d("onResponse: 성공: ", response.body() + response.message())

                                val jsonObject = JSONObject(response.body().toString())
                                Log.d(TAG, jsonObject.toString())
                                val profile_image = jsonObject.getString("profile")

                                val builder: AlertDialog.Builder = AlertDialog.Builder(this@ProfileEditActivity)
                                builder
                                    .setTitle("수정 완료")
                                    .setCancelable(false)
                                builder.setPositiveButton("확인", DialogInterface.OnClickListener { dialog, which ->
                                    LoginActivity.user_info.loginUserProfile = profile_image
                                    LoginActivity.user_info.loginUserNickname = updateNickname
                                    finish()
                                })
                                builder.show()
                            } else {
                                Log.e("onResponse", "실패 : " + response.errorBody())
                            }
                        }
                        override fun onFailure(call: Call<String>, t: Throwable) {
                            Log.d(
                                "실패:", "Failed API call with call: " + call + " + exception: " + t
                            )
                        }
                    })
            }else{
                var updateNickname = tv_nickName.text.toString()
                retrofitService.requestEditUserNickname(email!!, updateNickname)
                    .enqueue(object : Callback<String> {
                        override fun onResponse(
                            call: Call<String>,
                            response: Response<String>
                        ) {
                            if (response.isSuccessful) {
                                Log.d("onResponse: 성공: ", response.body() + response.message())
                                val jsonObject = JSONObject(response.body().toString())
                                Log.d(TAG, jsonObject.toString())

                                val builder: AlertDialog.Builder = AlertDialog.Builder(this@ProfileEditActivity)
                                builder
                                    .setTitle("수정 완료")
                                    .setCancelable(false)
                                builder.setPositiveButton("확인", DialogInterface.OnClickListener { dialog, which ->
                                    LoginActivity.user_info.loginUserNickname = tv_nickName.text.toString()
                                    finish()
                                })
                                builder.show()

                            } else {
                                Log.e("onResponse", "실패 : " + response.errorBody())
                            }
                        }

                        override fun onFailure(call: Call<String>, t: Throwable) {
                            Log.d(
                                "실패:", "Failed API call with call: " + call + " + exception: " + t
                            )
                        }
                    })
            }
//        }

    }
    fun settingPermission() {
        var permis = object  : PermissionListener {
            // 어떠한 형식을 상속받는 익명 클래스의 객체를 생성하기 위해 다음과 같이 작성
            override fun onPermissionGranted() {
                val builder = AlertDialog.Builder(this@ProfileEditActivity)
                builder.setTitle("업로드할 이미지 선택")
                builder.setPositiveButton("카메라로 사진찍기") { dialogInterface, i -> startCapture()
                }
                builder.setNeutralButton("앨범에서 사진선택") { dialogInterface, i -> openGalleryForImage()
                }
                builder.show()
            }

            override fun onPermissionDenied(deniedPermissions: MutableList<String>?) {
                Toast.makeText(this@ProfileEditActivity, "권한 거부", Toast.LENGTH_SHORT)
                    .show()
//                ActivityCompat.finishAffinity(this@ProfileEditActivity) // 권한 거부시 앱 종료
                finish()
            }
        }

        TedPermission.with(this)
            .setPermissionListener(permis)
            .setRationaleMessage("카메라 사진 권한 필요")
            .setDeniedMessage("카메라 권한 요청 거부")
            .setPermissions(
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
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
    private fun openGalleryForImage() {

        val intent= Intent(Intent.ACTION_PICK)
        intent.type= MediaStore.Images.Media.CONTENT_TYPE
        //createImageFile()
        startActivityForResult(intent, REQUEST_GALLERY_TAKE)
    }

    //
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val timeStamp : String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        if(requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK){
            isProfileImageEdit =true
            val file = File(currentPhotoPath)
            imageFile=file
            // Create an image file name
            fileName = "JPEG_$timeStamp.jpg"

            if (Build.VERSION.SDK_INT < 28) {
                val bitmap = MediaStore.Images.Media
                    .getBitmap(contentResolver, Uri.fromFile(file))
                iv_profileImage.setImageBitmap(bitmap)
            }
            else{
                val decode = ImageDecoder.createSource(
                    this.contentResolver,
                    Uri.fromFile(file)
                )
                val bitmap = ImageDecoder.decodeBitmap(decode)
                iv_profileImage.setImageBitmap(bitmap)
            }
        }else if(requestCode == REQUEST_GALLERY_TAKE && resultCode == Activity.RESULT_OK){
            isProfileImageEdit =true
            data?.data?.let{ uri ->
                val selectedImage = uri
                val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)
                val cursor = contentResolver.query(
                    selectedImage!!,
                    filePathColumn,
                    null,
                    null,
                    null
                )
                assert(cursor != null)
                cursor!!.moveToFirst()

                val columnIndex = cursor.getColumnIndex(filePathColumn[0])
                mediaPath = cursor.getString(columnIndex)
                iv_profileImage.setImageURI(uri)
                cursor.close()

                val file = File(mediaPath)
                imageFile = file
                fileName = "JPEG_$timeStamp.jpg"
                Log.e("갤러리 imageFile: ", imageFile.toString())
                iv_profileImage.setImageURI(uri)
            }
        }
    }

    private fun initRetrofit(){
        retrofit= RetrofitClient.getInstance()
        retrofitService=retrofit.create(RetrofitService::class.java)
    }

}