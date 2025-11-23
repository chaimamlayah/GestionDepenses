package isetb.prjtinteg.gestiondepenses

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class AffichageActivity : AppCompatActivity() {

    private lateinit var adapter: DepensesAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var tvEmpty: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_affichage)

        recyclerView = findViewById(R.id.recyclerDepenses)
        tvEmpty = findViewById(R.id.tvEmpty)
        val retour=findViewById<ImageView>(R.id.retour)

        retour.setOnClickListener {
            val i= Intent(this, HomeActivity::class.java)
            startActivity(i)
        }


        findViewById<View>(R.id.logout).setOnClickListener {
            getSharedPreferences("prefs", MODE_PRIVATE).edit().clear().apply()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        findViewById<View>(R.id.btn_graphe).setOnClickListener {
            startActivity(Intent(this, ChartsActivity::class.java))
        }


        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = DepensesAdapter()
        recyclerView.adapter = adapter

        chargerDepenses()
    }

    private fun chargerDepenses() {
        val db = DepensesDatabase(this)
        val liste = db.getAllDepenses()

        if (liste.isEmpty()) {
            tvEmpty.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        } else {
            tvEmpty.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
            adapter.submitList(liste)
        }
    }

    override fun onResume() {
        super.onResume()
        chargerDepenses()
    }
}