package isetb.prjtinteg.gestiondepenses

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textview.MaterialTextView

class MainActivity : AppCompatActivity() {

    private lateinit var userDb: UserDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Vérifier si l'utilisateur est déjà connecté
        val sp = getSharedPreferences("prefs", MODE_PRIVATE)
        if (sp.getBoolean("ETAT", false)) {
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
            return
        }

        setContentView(R.layout.activity_main)

        userDb = UserDatabase(this)

        val emailField = findViewById<TextInputEditText>(R.id.email)
        val passwordField = findViewById<TextInputEditText>(R.id.mdp)
        val btnConnexion = findViewById<MaterialButton>(R.id.btn_conn)
        val tvCreerCompte = findViewById<MaterialTextView>(R.id.btn_compte)

        // Aller vers l'inscription
        tvCreerCompte.setOnClickListener {
            startActivity(Intent(this, CompteActivity::class.java))
        }

        // Connexion
        btnConnexion.setOnClickListener {
            val email = emailField.text.toString().trim()
            val password = passwordField.text.toString()

            when {
                email.isEmpty() || password.isEmpty() -> {
                    Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_LONG).show()
                }
                !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                    Toast.makeText(this, "Email invalide", Toast.LENGTH_LONG).show()
                }
                else -> {
                    val user = userDb.checkUser(email, password)
                    if (user != null) {
                        // Sauvegarder la session
                        val editor = sp.edit()
                        editor.putBoolean("ETAT", true)
                        editor.putInt("ID", user.id)
                        editor.putString("NOM", user.nom_prenom)
                        editor.putString("EMAIL", user.email)
                        editor.apply()

                        Toast.makeText(this, "Connexion réussie ! Bienvenue ${user.nom_prenom}", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, HomeActivity::class.java))
                        finish()
                    } else {
                        Toast.makeText(this, "Email ou mot de passe incorrect", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }
}