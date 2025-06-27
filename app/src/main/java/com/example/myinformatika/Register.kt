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
import at.favre.lib.crypto.bcrypt.BCrypt
import com.google.android.material.textfield.TextInputEditText
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class Register : AppCompatActivity() {

    private val TAG = "RegisterActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.register)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val emailField = findViewById<TextInputEditText>(R.id.register_email)
        val usernameField = findViewById<TextInputEditText>(R.id.register_username)
        val studentIdField = findViewById<TextInputEditText>(R.id.student_id)
        val passwordField = findViewById<TextInputEditText>(R.id.register_password)
        val showPasswordCheckbox = findViewById<CheckBox>(R.id.show_register_password_checkbox)
        val registerButton = findViewById<Button>(R.id.register_button)
        val loading = findViewById<ProgressBar>(R.id.register_loading)
        val loginLink = findViewById<TextView>(R.id.login_link)

        loginLink.setOnClickListener {
            startActivity(Intent(this, Login::class.java))
        }

        passwordField.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        showPasswordCheckbox.setOnCheckedChangeListener { _, isChecked ->
            passwordField.inputType = if (isChecked) {
                InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            } else {
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            }
            passwordField.setSelection(passwordField.text?.length ?: 0)
        }

        registerButton.setOnClickListener {
            val email = emailField.text.toString().trim()
            val username = usernameField.text.toString().trim()
            val studentId = studentIdField.text.toString().trim()
            val password = passwordField.text.toString().trim()

            if (email.isEmpty() || username.isEmpty() || studentId.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Semua field wajib diisi", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            loading.visibility = View.VISIBLE

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val supabase = SupabaseClientProvider.client

                    val result = supabase.auth.signUpWith(Email) {
                        this.email = email
                        this.password = password
                    }

                    if (result != null) {

                        val hashedPassword = BCrypt.withDefaults().hashToString(12, password.toCharArray())
                        val insertResult = supabase.from("user").insert(
                            mapOf(
                                "id" to result.id,
                                "email" to email,
                                "username" to username,
                                "student_id" to studentId,
                                "password" to hashedPassword
                            )
                        )
                        Log.d(TAG, "Insert result: $insertResult")

                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@Register, "Registrasi berhasil!", Toast.LENGTH_SHORT).show()

                            // Arahkan ke halaman login
                            val intent = Intent(this@Register, Login::class.java)
                            startActivity(intent)
                            finish()
                        }

                    } else {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                this@Register,
                                "Registrasi berhasil. Silakan verifikasi email sebelum login.",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }

                } catch (e: Exception) {
                    Log.e(TAG, "Register gagal", e)
                    withContext(Dispatchers.Main) {
                        loading.visibility = View.GONE
                        Toast.makeText(this@Register, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }

        }
    }
}
