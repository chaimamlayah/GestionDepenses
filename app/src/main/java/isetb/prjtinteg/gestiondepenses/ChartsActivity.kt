package isetb.prjtinteg.gestiondepenses

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.android.material.snackbar.Snackbar

class ChartsActivity : AppCompatActivity() {

    private lateinit var db: DepensesDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_charts)

        db = DepensesDatabase(this)

        val retour=findViewById<ImageView>(R.id.retour)

        retour.setOnClickListener {
            val i= Intent(this, AffichageActivity::class.java)
            startActivity(i)
        }


        findViewById<View>(R.id.logout).setOnClickListener {
            getSharedPreferences("prefs", MODE_PRIVATE).edit().clear().apply()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
        chargerStatistiquesEtAlertes()
    }

    private fun chargerStatistiquesEtAlertes() {
        val depensesCeMois = db.getDepensesMoisEnCours()
        val totalCeMois = depensesCeMois.sumOf { it.montant.toDouble() }.toFloat()

        // Affichage du total
        findViewById<TextView>(R.id.tvTotalMontant).text =
            if (totalCeMois > 0) "%.2f DT".format(totalCeMois)
            else "Aucune dépense ce mois"

        if (depensesCeMois.isEmpty()) return

        val salaire = getSharedPreferences("prefs", MODE_PRIVATE).getFloat("SALAIRE", 0f)

        // Alerte : dépassement du salaire
        if (salaire > 0 && totalCeMois > salaire) {
            montrerAlerte("Budget dépassé !", "Vous avez dépensé plus que votre salaire !", Color.RED)
        }

        // Alerte : +30% vs mois dernier
        val totalMoisDernier = db.getTotalMoisPrecedent()
        if (totalMoisDernier > 0 && totalCeMois > totalMoisDernier * 1.3f) {
            montrerAlerte("Dépenses en hausse !", "+30% par rapport au mois dernier", Color.rgb(255, 152, 0))
        }

        // Analyse par catégorie
        val parCategorie = depensesCeMois.groupBy { it.categorie }
            .mapValues { it.value.sumOf { d -> d.montant.toDouble() }.toFloat() }

        parCategorie.forEach { (categorie, montant) ->
            // Plus de 30% du salaire ?
            if (salaire > 0 && montant > salaire * 0.3f) {
                montrerAlerte("Catégorie excessive", "$categorie : ${montant.toInt()} DT (>30% du salaire)", Color.MAGENTA)
            }

            // Plus de 50% vs mois dernier ?
            val montantMoisDernier = db.getMontantCategorieMoisPrecedent(categorie)
            if (montantMoisDernier > 0 && montant > montantMoisDernier * 1.5f) {
                montrerAlerte("Attention $categorie", "+50% vs le mois dernier !", Color.BLUE)
            }
        }

        // Chargement des graphiques
        configurerBarChart(parCategorie)
        configurerPieChart(parCategorie)
    }

    private fun montrerAlerte(titre: String, message: String, couleur: Int) {
        Snackbar.make(findViewById(android.R.id.content), "$titre → $message", Snackbar.LENGTH_LONG)
            .setBackgroundTint(couleur)
            .setTextColor(Color.WHITE)
            .setAnimationMode(Snackbar.ANIMATION_MODE_SLIDE)
            .show()
    }

    private fun configurerBarChart(dataMap: Map<String, Float>) {
        val barChart = findViewById<BarChart>(R.id.barChart)
        val entries = dataMap.entries.mapIndexed { i, e -> BarEntry(i.toFloat(), e.value) }
        val dataSet = BarDataSet(entries, "").apply {
            colors = ColorTemplate.MATERIAL_COLORS.toList()
            valueTextSize = 13f
            valueTextColor = Color.BLACK
        }

        barChart.data = BarData(dataSet).apply { barWidth = 0.6f }
        barChart.description.isEnabled = false
        barChart.legend.isEnabled = false
        barChart.setFitBars(true)
        barChart.xAxis.apply {
            position = XAxis.XAxisPosition.BOTTOM
            granularity = 1f
            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float) = dataMap.keys.elementAtOrNull(value.toInt()) ?: ""
            }
        }
        barChart.axisLeft.axisMinimum = 0f
        barChart.axisRight.isEnabled = false
        barChart.animateY(1200, Easing.EaseInOutQuad)
        barChart.invalidate()
    }

    private fun configurerPieChart(dataMap: Map<String, Float>) {
        val pieChart = findViewById<PieChart>(R.id.pieChart)
        val entries = dataMap.map { PieEntry(it.value, it.key) }
        val dataSet = PieDataSet(entries, "").apply {
            colors = ColorTemplate.MATERIAL_COLORS.toList()
            valueTextSize = 14f
            valueTextColor = Color.WHITE
            sliceSpace = 3f
        }

        pieChart.data = PieData(dataSet).apply {
            setValueFormatter(object : ValueFormatter() {
                override fun getFormattedValue(value: Float) = "%.0f DT".format(value)
            })
        }
        pieChart.centerText = "Ce mois"
        pieChart.setCenterTextSize(18f)
        pieChart.description.isEnabled = false
        pieChart.legend.isEnabled = true
        pieChart.animateY(1400, Easing.EaseInOutQuad)
        pieChart.invalidate()
    }
}