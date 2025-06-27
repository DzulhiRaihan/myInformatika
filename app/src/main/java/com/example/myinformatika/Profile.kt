package com.example.myinformatika

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
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

class Profile : AppCompatActivity() {

    private lateinit var profileImage: ImageView
    private lateinit var profileName: TextView
    private lateinit var profileStudentId: TextView
    private lateinit var logoutButton: Button
    private lateinit var choosePhoto: TextView
    private lateinit var editProfileButton: Button
    private lateinit var articleButton: Button

    private var currentUserId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val currentUser = SupabaseClientProvider.client.auth.currentUserOrNull()
        if (currentUser == null) {
            Toast.makeText(this, "Silakan login terlebih dahulu", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, Login::class.java))
            finish()
            return
        }

        enableEdgeToEdge()
        setContentView(R.layout.activity_profile)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.profile)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        profileImage = findViewById(R.id.profile_image)
        profileName = findViewById(R.id.profile_name)
        profileStudentId = findViewById(R.id.student_id)
        logoutButton = findViewById(R.id.logout_button)
        choosePhoto = findViewById(R.id.choose_photo)
        editProfileButton = findViewById(R.id.edit_profile_button)
        articleButton = findViewById(R.id.article_button)

        choosePhoto.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        logoutButton.setOnClickListener {
            logout()
        }

        editProfileButton.setOnClickListener {
            editProfile()
        }

        articleButton.setOnClickListener {
            startActivity(Intent(this, Article::class.java))
        }

        loadUserData()
    }

    override fun onResume() {
        super.onResume()
        loadUserData()
    }

    private fun loadUserData() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val supabase = SupabaseClientProvider.client
                val currentUser = supabase.auth.currentUserOrNull()

                if (currentUser != null) {
                    val userId = currentUser.id
                    currentUserId = userId

                    val response = supabase.from("user").select {
                        filter {
                            eq("id", userId)
                        }
                        limit(1)
                    }.decodeList<User>()

                    val user = response.firstOrNull()

                    withContext(Dispatchers.Main) {
                        if (user != null) {
                            profileName.text = getString(R.string.greeting, user.username ?: "User")
                            profileStudentId.text = getString(R.string.profile_student_id, user.student_id ?: "NIM")
                            Glide.with(this@Profile)
                                .load(user.photo_URL)
                                .skipMemoryCache(true)
                                .diskCacheStrategy(DiskCacheStrategy.NONE)
                                .placeholder(R.drawable.baseline_add_a_photo_24)
                                .circleCrop()
                                .into(profileImage)
                        } else {
                            Toast.makeText(this@Profile, "Data user tidak ditemukan", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("Profile", "Gagal memuat data user", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@Profile, "Gagal memuat profil: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun logout() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                SupabaseClientProvider.client.auth.signOut()
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@Profile, "Logout berhasil", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this@Profile, Login::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@Profile, "Gagal logout: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun editProfile() {
        val intent = Intent(this, EditProfile::class.java)
        intent.putExtra("userId", currentUserId)
        startActivity(intent)
    }

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { uploadProfilePicture(it) }
    }

    private fun uploadProfilePicture(uri: Uri) {
        val supabase = SupabaseClientProvider.client

        val userId = currentUserId
        if (userId == null) {
            Toast.makeText(this, "User tidak ditemukan", Toast.LENGTH_SHORT).show()
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val inputStream = contentResolver.openInputStream(uri)
                val bytes = inputStream?.readBytes() ?: return@launch
                val fileName = "$userId.jpg"

                supabase.storage.from("profile-picture")
                    .upload(fileName, data = bytes) {
                        upsert = true
                    }

                val publicUrl = supabase.storage.from("profile-picture").publicUrl(fileName)

                supabase.from("user")
                    .update(
                        {
                            set("photo_URL", publicUrl)
                        }
                    ) {
                        filter {
                            eq("id", userId)
                        }
                    }

                withContext(Dispatchers.Main) {
                    Toast.makeText(this@Profile, "Foto berhasil diunggah", Toast.LENGTH_SHORT).show()
                    // Muat ulang seluruh data user, termasuk gambar
                    loadUserData()
                }

            } catch (e: Exception) {
                Log.e("UploadPhoto", "Gagal upload", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@Profile, "Upload gagal: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}
