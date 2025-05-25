package com.example.mymindv2

import android.Manifest
import android.app.DatePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Outline
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.view.View
import android.view.ViewOutlineProvider
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.mymindv2.databinding.ActivityRegisterBinding
import java.io.InputStream
import java.util.Calendar

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private var selectedBirthday: String? = null
    private var selectedBirthdayCalendar: Calendar? = null

    private val imagePickerLauncher =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
            uri?.let {
                contentResolver.takePersistableUriPermission(
                    it,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                processAndSetImage(it)
            } ?: Toast.makeText(this, "No se seleccionó ninguna imagen", Toast.LENGTH_SHORT).show()
        }

    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) pickImage() else Toast.makeText(
                this,
                "Permiso denegado",
                Toast.LENGTH_SHORT
            ).show()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.registerButton.setOnClickListener { validateInputs() }
        binding.loginButton.setOnClickListener {
            startActivity(Intent(this, SignInActivity::class.java))
            finish()
        }

        binding.profileImageView.apply {
            outlineProvider = object : ViewOutlineProvider() {
                override fun getOutline(view: View, outline: Outline) {
                    outline.setOval(0, 0, view.width, view.height)
                }
            }
            clipToOutline = true
        }

        binding.imageFrameLayout.setOnClickListener { requestPermissionsAndPickImage() }

        val passwordEditText = findViewById<EditText>(R.id.passwordEditText)
        val togglePasswordButton = findViewById<ImageButton>(R.id.togglePasswordButton)

        togglePasswordButton.setOnClickListener {
            if (passwordEditText.transformationMethod is PasswordTransformationMethod) {
                passwordEditText.transformationMethod = null
                togglePasswordButton.setImageResource(R.drawable.baseline_visibility_24)
            } else {
                passwordEditText.transformationMethod = PasswordTransformationMethod.getInstance()
                togglePasswordButton.setImageResource(R.drawable.baseline_visibility_off_24)
            }
            passwordEditText.setSelection(passwordEditText.text.length)
        }

        //Spinners
        val genderAdapter = ArrayAdapter.createFromResource(
            this, R.array.gender_options, R.layout.spinner_item_purple
        )
        genderAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item_purple)
        binding.spinnerGender.adapter = genderAdapter

        val personalityAdapter = ArrayAdapter.createFromResource(
            this, R.array.personality_options, R.layout.spinner_item_purple
        )
        personalityAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item_purple)
        binding.spinnerPersonality.adapter = personalityAdapter


        //Password
        binding.passwordEditText.setOnFocusChangeListener { _, hasFocus ->
            binding.passwordInstructionsLayout.visibility = if (hasFocus) View.VISIBLE else View.GONE
        }

        binding.passwordEditText.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: android.text.Editable?) {
                val password = s.toString()

                binding.tvLength.visibility = if (password.length >= 8) View.GONE else View.VISIBLE
                binding.tvUpper.visibility = if (password.any { it.isUpperCase() }) View.GONE else View.VISIBLE
                binding.tvLower.visibility = if (password.any { it.isLowerCase() }) View.GONE else View.VISIBLE
                binding.tvDigit.visibility = if (password.any { it.isDigit() }) View.GONE else View.VISIBLE
                binding.tvSpecial.visibility = if (password.any { "!@#\$%&*?+-_/".contains(it) }) View.GONE else View.VISIBLE
            }
        })



        binding.birthdayButton.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePicker = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
                val selectedDate = Calendar.getInstance().apply {
                    set(selectedYear, selectedMonth, selectedDay)
                }

                val today = Calendar.getInstance()
                if (selectedDate.after(today)) {
                    Toast.makeText(this, "La fecha no puede ser en el futuro.", Toast.LENGTH_LONG).show()
                    return@DatePickerDialog
                }

                val minAgeDate = Calendar.getInstance().apply {
                    add(Calendar.YEAR, -18)
                }

                if (selectedDate.after(minAgeDate)) {
                    Toast.makeText(this, "Debes tener al menos 18 años para registrarte.", Toast.LENGTH_LONG).show()
                    return@DatePickerDialog
                }

                selectedBirthdayCalendar = selectedDate
                selectedBirthday = "$selectedDay/${selectedMonth + 1}/$selectedYear"
                binding.birthdayButton.text = selectedBirthday

            }, year, month, day)

            datePicker.show()
        }
    }

    private fun validateInputs() {
        val name = binding.nameEditText.text.toString().trim()
        val email = binding.emailEditText.text.toString().trim()
        val password = binding.passwordEditText.text.toString().trim()
        val imageUri = binding.profileImageView.tag as? Uri
        val city = binding.editTextCity.text.toString().trim()
        val university = binding.editTextUniversidad.text.toString().trim()
        val career = binding.editTextCareer.text.toString().trim()
        val gender = binding.spinnerGender.selectedItem.toString()
        val personality = binding.spinnerPersonality.selectedItem.toString()

        val errors = mutableListOf<String>()

        if (!name.matches(Regex("^[a-zA-Z ]+\$"))) errors.add("El nombre no debe contener números.")
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) errors.add("Correo inválido.")
        if (!password.matches(Regex("^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@\$!%*#?&+\\-_/])[A-Za-z\\d@\$!%*#?&+\\-_/]{8,}\$"))) {
            errors.add("La contraseña debe tener al menos 8 caracteres, una mayúscula, un número y un carácter especial.")
        }
        if (imageUri == null) errors.add("Por favor, selecciona una imagen de perfil.")

        if (name.isEmpty() || email.isEmpty() || password.isEmpty() ||
            city.isEmpty() || university.isEmpty() || career.isEmpty() ||
            selectedBirthday == null || gender == "Selecciona tu género" || personality == "Selecciona tu personalidad"
        ) {
            Toast.makeText(this, "Por favor completa todos los campos.", Toast.LENGTH_SHORT).show()
            return
        }
        if (selectedBirthdayCalendar == null) {
            Toast.makeText(this, "Por favor selecciona una fecha de nacimiento válida.", Toast.LENGTH_SHORT).show()
            return
        }

        if (errors.isNotEmpty()) {
            Toast.makeText(this, errors.joinToString("\n"), Toast.LENGTH_LONG).show()
            return
        }

        val intent = Intent(this, PrivacyAgreeActivity::class.java).apply {
            putExtra("name", name)
            putExtra("email", email)
            putExtra("password", password)
            putExtra("profileImageUri", imageUri.toString())
            putExtra("city", city)
            putExtra("university", university)
            putExtra("career", career)
            putExtra("gender", when (gender) {
                "Otro" -> "Omitido"
                else -> gender
            })
            putExtra("personality", personality)
            putExtra("birthday", selectedBirthday)
        }

        Log.d("RegisterActivity", "Selected Image URI: $imageUri")
        startActivity(intent)
    }

    private fun requestPermissionsAndPickImage() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        when {
            ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED -> pickImage()
            shouldShowRequestPermissionRationale(permission) -> {
                Toast.makeText(this, "Se necesita permiso para seleccionar una imagen", Toast.LENGTH_LONG).show()
                permissionLauncher.launch(permission)
            }
            else -> permissionLauncher.launch(permission)
        }
    }

    private fun pickImage() {
        imagePickerLauncher.launch(arrayOf("image/*"))
    }

    private fun processAndSetImage(uri: Uri) {
        try {
            val inputStream = contentResolver.openInputStream(uri)
            val originalBitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            if (originalBitmap != null) {
                // Leer orientación EXIF
                val orientation = contentResolver.openInputStream(uri)?.use { input ->
                    androidx.exifinterface.media.ExifInterface(input).getAttributeInt(
                        androidx.exifinterface.media.ExifInterface.TAG_ORIENTATION,
                        androidx.exifinterface.media.ExifInterface.ORIENTATION_NORMAL
                    )
                } ?: androidx.exifinterface.media.ExifInterface.ORIENTATION_NORMAL

                val rotatedBitmap = when (orientation) {
                    androidx.exifinterface.media.ExifInterface.ORIENTATION_ROTATE_90 -> rotateBitmap(originalBitmap, 90f)
                    androidx.exifinterface.media.ExifInterface.ORIENTATION_ROTATE_180 -> rotateBitmap(originalBitmap, 180f)
                    androidx.exifinterface.media.ExifInterface.ORIENTATION_ROTATE_270 -> rotateBitmap(originalBitmap, 270f)
                    else -> originalBitmap
                }

                val imageViewSize = resources.getDimensionPixelSize(R.dimen.profile_image_size)
                val size = minOf(rotatedBitmap.width, rotatedBitmap.height)

                val croppedBitmap = Bitmap.createBitmap(
                    rotatedBitmap,
                    (rotatedBitmap.width - size) / 2,
                    (rotatedBitmap.height - size) / 2,
                    size,
                    size
                )

                val scaledBitmap = Bitmap.createScaledBitmap(croppedBitmap, imageViewSize, imageViewSize, true)

                runOnUiThread {
                    binding.profileImageView.setImageBitmap(scaledBitmap)
                    binding.uploadIcon.visibility = View.GONE
                    binding.profileImageView.tag = uri
                }
            } else {
                Toast.makeText(this, "Error al procesar la imagen", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error al cargar la imagen", Toast.LENGTH_SHORT).show()
        }
    }

    private fun rotateBitmap(bitmap: Bitmap, degrees: Float): Bitmap {
        val matrix = android.graphics.Matrix()
        matrix.postRotate(degrees)
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }


}