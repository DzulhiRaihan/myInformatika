package com.example.myinformatika

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.textfield.TextInputEditText
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class Login : AppCompatActivity() {

    private val TAG = "LoginActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val emailOrNimField = findViewById<TextInputEditText>(R.id.username)
        val passwordField = findViewById<TextInputEditText>(R.id.password)
        val loginButton = findViewById<Button>(R.id.login_button)
        val loading = findViewById<ProgressBar>(R.id.loading)
        val showPasswordCheckbox = findViewById<CheckBox>(R.id.show_password_checkbox)
        val registerLink = findViewById<TextView>(R.id.register_link)

        registerLink.setOnClickListener {
            startActivity(Intent(this, Register::class.java))
        }
        // Show/Hide password
        passwordField.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        showPasswordCheckbox.setOnCheckedChangeListener { _, isChecked ->
            passwordField.inputType = if (isChecked) {
                InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            } else {
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            }
            passwordField.setSelection(passwordField.text?.length ?: 0)
        }

        loginButton.setOnClickListener {
            val identifier = emailOrNimField.text.toString().trim()
            val password = passwordField.text.toString().trim()


            if (identifier.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Field tidak boleh kosong", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            loading.visibility = View.VISIBLE

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val supabase = SupabaseClientProvider.client
                    val response = supabase.from("user").select {
                        filter {
                            or {
                                eq("email", identifier)
                                eq("student_id", identifier)
                            }
                        }
                        limit(1)
                    }.decodeList<User>()

                    if (response.isNotEmpty()) {
                        val email = response[0].email

                        // Login menggunakan email dan password
                        val result = supabase.auth.signInWith(Email) {
                            this.email = email?: identifier
                            this.password = password
                        }

                        withContext(Dispatchers.Main) {
                            loading.visibility = View.GONE
                            Toast.makeText(this@Login, "Login berhasil!", Toast.LENGTH_SHORT).show()
                            val session = supabase.auth.currentSessionOrNull()
                            Log.d("Login", "Session: $session")
                            // Arahkan ke halaman utama
                            startActivity(Intent(this@Login, Profile::class.java))
                            finish()
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            loading.visibility = View.GONE
                            Toast.makeText(this@Login, "User tidak ditemukan", Toast.LENGTH_SHORT).show()
                        }
                    }

                } catch (e: Exception) {
                    Log.e(TAG, "Login gagal", e)
                    withContext(Dispatchers.Main) {
                        loading.visibility = View.GONE
                        Toast.makeText(this@Login, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }
}
