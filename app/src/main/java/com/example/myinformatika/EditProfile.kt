package com.example.myinformatika

import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.*

class EditProfile : AppCompatActivity() {

    private lateinit var profileImage: ImageView
    private lateinit var choosePhotoText: TextView
    private lateinit var usernameField: EditText
    private lateinit var studentIdField: EditText
    private lateinit var saveButton: Button
    private lateinit var backButton: Button

    private var selectedImageUri: Uri? = null
    private var currentPhotoUrl: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_edit_profile)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.edit_profile)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        profileImage = findViewById(R.id.edit_profile_image)
        choosePhotoText = findViewById(R.id.edit_choose_photo)
        usernameField = findViewById(R.id.edit_username)
        studentIdField = findViewById(R.id.edit_student_id)
        saveButton = findViewById(R.id.save_edit_button)
        backButton = findViewById(R.id.back_button)

        choosePhotoText.setOnClickListener {
            imagePicker.launch("image/*")
        }

        saveButton.setOnClickListener {
            updateProfile()
        }

        backButton.setOnClickListener {
            finish()
        }

        loadUserData()
    }

    private fun loadUserData() {
        CoroutineScope(Dispatchers.IO).launch {
            val supabase = SupabaseClientProvider.client
            val user = supabase.auth.currentUserOrNull() ?: return@launch

            val response = supabase.from("user").select {
                filter { eq("id", user.id) }
                limit(1)
            }.decodeList<User>()

            val userData = response.firstOrNull() ?: return@launch

            withContext(Dispatchers.Main) {
                usernameField.setText(userData.username)
                studentIdField.setText(userData.student_id)
                currentPhotoUrl = userData.photo_URL

                Glide.with(this@EditProfile)
                    .load(userData.photo_URL)
                    .skipMemoryCache(true)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .placeholder(R.drawable.baseline_add_a_photo_24)
                    .circleCrop()
                    .into(profileImage)
            }
        }
    }

    private fun updateProfile() {
        val supabase = SupabaseClientProvider.client
        val user = supabase.auth.currentUserOrNull()

        if (user == null) {
            Toast.makeText(this, "User tidak ditemukan", Toast.LENGTH_SHORT).show()
            return
        }

        val username = usernameField.text.toString().trim()
        val studentId = studentIdField.text.toString().trim()

        if (username.isBlank() || studentId.isBlank()) {
            Toast.makeText(this, "Username dan NIM tidak boleh kosong", Toast.LENGTH_SHORT).show()
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            var newPhotoUrl = currentPhotoUrl

            selectedImageUri?.let {
                val bytes = contentResolver.openInputStream(it)?.readBytes()
                val fileName = "${user.id}.jpg"
                supabase.storage.from("profile-picture")
                    .upload(path = fileName, data = bytes!!) {
                        upsert = true
                    }
                newPhotoUrl = supabase.storage.from("profile-picture").publicUrl(fileName)
            }

            supabase.from("user").update(
                mapOf(
                    "username" to username,
                    "student_id" to studentId,
                    "photo_URL" to newPhotoUrl
                )
            ) {
                filter { eq("id", user.id) }
            }

            withContext(Dispatchers.Main) {
                Toast.makeText(this@EditProfile, "Profil berhasil diperbarui", Toast.LENGTH_SHORT).show()
                setResult(RESULT_OK)
                finish()
            }
        }
    }

    private val imagePicker = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            selectedImageUri = it
            profileImage.setImageURI(it)
        }
    }
}
