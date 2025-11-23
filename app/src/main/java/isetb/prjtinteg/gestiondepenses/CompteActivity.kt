package isetb.prjtinteg.gestiondepenses

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textview.MaterialTextView

class CompteActivity : AppCompatActivity() {

    private lateinit var db: UserDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_compte)

        db = UserDatabase(this)

        val nomUser = findViewById<TextInputEditText>(R.id.nom_user)
        val email = findViewById<TextInputEditText>(R.id.comp_email)
        val mdp = findViewById<TextInputEditText>(R.id.compte_mdp)
        val conf = findViewById<TextInputEditText>(R.id.conf)
        val btnEnregistrer = findViewById<MaterialButton>(R.id.btn_enregistrer)
        val tvConnexion = findViewById<MaterialTextView>(R.id.conn)

        tvConnexion.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        btnEnregistrer.setOnClickListener {
            val nom = nomUser.text.toString().trim()
            val mail = email.text.toString().trim()
            val password = mdp.text.toString()
            val confirmPassword = conf.text.toString()

            when {
                nom.isEmpty() || mail.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() -> {
                    Toast.makeText(this, "Tous les champs sont obligatoires", Toast.LENGTH_LONG).show()
                }
                password != confirmPassword -> {
                    Toast.makeText(this, "Les mots de passe ne correspondent pas", Toast.LENGTH_LONG).show()
                }
                password.length < 6 -> {
                    Toast.makeText(this, "Le mot de passe doit faire au moins 6 caractères", Toast.LENGTH_LONG).show()
                }
                !android.util.Patterns.EMAIL_ADDRESS.matcher(mail).matches() -> {
                    Toast.makeText(this, "Email invalide", Toast.LENGTH_LONG).show()
                }
                else -> {
                    val newUser = User(nom, mail, password)
                    if (db.addUser(newUser)) {
                        // Connexion automatique après inscription
                        val user = db.checkUser(mail, password)
                        if (user != null) {
                            val sp = getSharedPreferences("prefs", MODE_PRIVATE).edit()
                            sp.putBoolean("ETAT", true)
                            sp.putInt("ID", user.id)
                            sp.putString("NOM", user.nom_prenom)
                            sp.putString("EMAIL", user.email)
                            sp.apply()

                            Toast.makeText(this, "Compte créé avec succès !", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this, HomeActivity::class.java))
                            finish()
                        }
                    } else {
                        Toast.makeText(this, "Cet email est déjà utilisé", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }
}