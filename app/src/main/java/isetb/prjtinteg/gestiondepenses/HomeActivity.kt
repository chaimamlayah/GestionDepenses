package isetb.prjtinteg.gestiondepenses

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.icu.util.Calendar
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.TextInputEditText
import java.text.SimpleDateFormat
import java.util.*

class HomeActivity : AppCompatActivity() {

    private lateinit var db: DepensesDatabase

    private lateinit var categorieInput: EditText
    private lateinit var descriptionInput: EditText
    private lateinit var montantInput: EditText
    private lateinit var dateInput: TextInputEditText
    private lateinit var salaireInput: EditText   // Champ salaire

    private val PREFS_NAME = "prefs"
    private val CHANNEL_ID = "budget_channel"
    private val NOTIFICATION_ID = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)


        db = DepensesDatabase(this)


        categorieInput = findViewById(R.id.categorie)
        descriptionInput = findViewById(R.id.description)
        montantInput = findViewById(R.id.montant)
        dateInput = findViewById(R.id.dateInput)
        salaireInput = findViewById(R.id.salaire)

        val btnEnreg = findViewById<Button>(R.id.btn_enreg)
        val btnVoirTableau = findViewById<Button>(R.id.btn_tab)
        val btnLogout = findViewById<ImageView>(R.id.logout)


        createNotificationChannel()


        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val salaireSauvegarde = prefs.getFloat("SALAIRE", 0f)

        if (salaireSauvegarde > 0) {
            salaireInput.setText(salaireSauvegarde.toString())
        } else {
            Toast.makeText(this, "Entrez d'abord votre salaire mensuel !", Toast.LENGTH_LONG).show()
            salaireInput.requestFocus()
            btnEnreg.isEnabled = false
        }


        salaireInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val salaire = s.toString().toFloatOrNull() ?: 0f
                if (salaire > 0) {
                    prefs.edit().putFloat("SALAIRE", salaire).apply()
                    btnEnreg.isEnabled = true
                } else {
                    btnEnreg.isEnabled = false
                }
            }
        })


        dateInput.setOnClickListener { showDatePicker() }
        btnEnreg.setOnClickListener { enregistrerDepense() }
        btnVoirTableau.setOnClickListener {
            startActivity(Intent(this, AffichageActivity::class.java))
        }


        btnLogout.setOnClickListener {
            getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit().clear().apply()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    private fun showDatePicker() {
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Choisir une date")
            .build()

        datePicker.addOnPositiveButtonClickListener { selectedDate ->
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE)
            dateInput.setText(sdf.format(Date(selectedDate)))
        }
        datePicker.show(supportFragmentManager, "DATE_PICKER")
    }

    private fun enregistrerDepense() {
        val categorie = categorieInput.text.toString().trim()
        val description = descriptionInput.text.toString().trim()
        val montantStr = montantInput.text.toString().trim()
        val date = dateInput.text.toString().trim()

        if (categorie.isEmpty() || montantStr.isEmpty() || date.isEmpty()) {
            Toast.makeText(this, "Catégorie, montant et date obligatoires !", Toast.LENGTH_LONG).show()
            return
        }

        val montant = montantStr.toFloatOrNull() ?: run {
            Toast.makeText(this, "Montant invalide", Toast.LENGTH_LONG).show()
            return
        }

        val salaire = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).getFloat("SALAIRE", 0f)
        if (salaire <= 0) {
            Toast.makeText(this, "Veuillez d'abord saisir votre salaire", Toast.LENGTH_LONG).show()
            return
        }

        val depense = Depenses(0, categorie, description, montant, date)
        if (db.addDepense(depense)) {
            Toast.makeText(this, "Dépense enregistrée !", Toast.LENGTH_SHORT).show()
            verifierEtNotifier(categorie, montant)
            viderChamps()
        } else {
            Toast.makeText(this, "Erreur d'enregistrement", Toast.LENGTH_SHORT).show()
        }
    }

    private fun verifierEtNotifier(nouvelleCategorie: String, nouveauMontant: Float) {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val salaire = prefs.getFloat("SALAIRE", 0f)
        val toutesDepenses = db.getAllDepenses()

        // Dépenses du mois en cours
        val depensesCeMois = toutesDepenses.filter { estCeMois(it.date) }
        val totalCeMois = depensesCeMois.sumOf { it.montant.toDouble() }.toFloat()

        // 1. Dépassement du salaire total
        if (totalCeMois > salaire) {
            envoyerNotification("Budget dépassé !", "Vous avez dépensé plus que votre salaire ce mois-ci !")
        }

        // 2. Dépassement d'une catégorie (30% du salaire)
        val totalCategorie = depensesCeMois
            .filter { it.categorie.equals(nouvelleCategorie, ignoreCase = true) }
            .sumOf { it.montant.toDouble() }.toFloat()

        if (totalCategorie > salaire * 0.30f) {
            envoyerNotification(
                "Catégorie dépassée",
                "$nouvelleCategorie dépasse 30% de votre salaire ! (${"%.0f".format(totalCategorie)} DT)"
            )
        }

        // 3. Comparaison avec le mois précédent
        val depensesMoisPrecedent = toutesDepenses.filter { estMoisPrecedent(it.date) }
        val totalMoisPrecedent = depensesMoisPrecedent.sumOf { it.montant.toDouble() }.toFloat()

        if (totalMoisPrecedent > 0 && totalCeMois > totalMoisPrecedent * 1.2f) {
            Toast.makeText(this, "Vous dépensez +20% vs le mois dernier !", Toast.LENGTH_LONG).show()
        }
    }

    private fun estCeMois(dateStr: String): Boolean {
        return try {
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE)
            val date = sdf.parse(dateStr)!!
            val cal = Calendar.getInstance().apply { time = date }
            val now = Calendar.getInstance()
            cal.get(Calendar.MONTH) == now.get(Calendar.MONTH) &&
                    cal.get(Calendar.YEAR) == now.get(Calendar.YEAR)
        } catch (e: Exception) { false }
    }

    private fun estMoisPrecedent(dateStr: String): Boolean {
        return try {
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE)
            val date = sdf.parse(dateStr)!!
            val cal = Calendar.getInstance().apply { time = date }
            val now = Calendar.getInstance()
            now.add(Calendar.MONTH, -1)
            cal.get(Calendar.MONTH) == now.get(Calendar.MONTH) &&
                    cal.get(Calendar.YEAR) == now.get(Calendar.YEAR)
        } catch (e: Exception) { false }
    }

    private fun viderChamps() {
        categorieInput.text.clear()
        descriptionInput.text.clear()
        montantInput.text.clear()
        dateInput.text?.clear()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Alertes Budget",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications quand vous dépassez votre budget"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun envoyerNotification(titre: String, message: String) {
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_warning) // crée un drawable ic_warning ou utilise ton logo
            .setContentTitle(titre)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(this)) {
            notify(NOTIFICATION_ID + Random().nextInt(1000), builder.build())
        }
    }
}