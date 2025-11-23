package isetb.prjtinteg.gestiondepenses

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.text.SimpleDateFormat
import java.util.*

class DepensesDatabase(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_VERSION = 1
        private const val DATABASE_NAME = "depenses_db"
        private const val TABLE_NAME = "depenses"

        private const val COL_ID = "id"
        private const val COL_CATEGORIE = "categorie"
        private const val COL_DESCRIPTION = "description"
        private const val COL_MONTANT = "montant"
        private const val COL_DATE = "date"
    }

    private val CREATE_TABLE = """
        CREATE TABLE $TABLE_NAME (
            $COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
            $COL_CATEGORIE TEXT NOT NULL,
            $COL_DESCRIPTION TEXT,
            $COL_MONTANT REAL NOT NULL,
            $COL_DATE TEXT NOT NULL
        )
    """.trimIndent()

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(CREATE_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    fun addDepense(depense: Depenses): Boolean {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COL_CATEGORIE, depense.categorie)
            put(COL_DESCRIPTION, depense.description)
            put(COL_MONTANT, depense.montant)
            put(COL_DATE, depense.date)
        }
        val result = db.insert(TABLE_NAME, null, values)
        db.close()
        return result != -1L
    }

    fun getAllDepenses(): List<Depenses> {
        val list = mutableListOf<Depenses>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_NAME ORDER BY $COL_ID DESC", null)

        if (cursor.moveToFirst()) {
            do {
                list.add(cursorToDepense(cursor))
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return list
    }

    fun getDepensesMoisEnCours(): List<Depenses> {
        val list = mutableListOf<Depenses>()
        val db = this.readableDatabase
        val currentMonthYear = getCurrentMonthYear() // "MM/yyyy"
        val cursor = db.rawQuery(
            "SELECT * FROM $TABLE_NAME WHERE substr($COL_DATE, 4, 7) = ? ORDER BY $COL_ID DESC",
            arrayOf(currentMonthYear)
        )
        if (cursor.moveToFirst()) {
            do {
                list.add(cursorToDepense(cursor))
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return list
    }

    fun getTotalMoisEnCours(): Float {
        return getDepensesMoisEnCours().sumOf { it.montant.toDouble() }.toFloat()
    }

    fun getTotalCategorieMoisEnCours(categorie: String): Float {
        return getDepensesMoisEnCours()
            .filter { it.categorie.equals(categorie, ignoreCase = true) }
            .sumOf { it.montant.toDouble() }
            .toFloat()
    }

    private fun cursorToDepense(cursor: Cursor): Depenses {
        return Depenses(
            id = cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID)),
            categorie = cursor.getString(cursor.getColumnIndexOrThrow(COL_CATEGORIE)),
            description = cursor.getString(cursor.getColumnIndexOrThrow(COL_DESCRIPTION)) ?: "",
            montant = cursor.getFloat(cursor.getColumnIndexOrThrow(COL_MONTANT)),
            date = cursor.getString(cursor.getColumnIndexOrThrow(COL_DATE))
        )
    }

    private fun getCurrentMonthYear(): String {
        return SimpleDateFormat("MM/yyyy", Locale.getDefault()).format(Date())
    }

    private fun getLastMonthYear(): String {
        val cal = Calendar.getInstance()
        cal.add(Calendar.MONTH, -1)
        return SimpleDateFormat("MM/yyyy", Locale.getDefault()).format(cal.time)
    }


    fun supprimerDepense(id: Int): Boolean {
        val db = this.writableDatabase
        val result = db.delete(TABLE_NAME, "$COL_ID = ?", arrayOf(id.toString()))
        db.close()
        return result > 0
    }

    fun getTotalMoisPrecedent(): Float {
        val cal = Calendar.getInstance()
        cal.add(Calendar.MONTH, -1)
        val moisAnnee = SimpleDateFormat("MM/yyyy", Locale.FRANCE).format(cal.time)
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT SUM(montant) FROM depenses WHERE strftime('%m/%Y', date) = ?", arrayOf(moisAnnee))
        val total = if (cursor.moveToFirst() && !cursor.isNull(0)) cursor.getFloat(0) else 0f
        cursor.close()
        return total
    }

    fun getMontantCategorieMoisPrecedent(categorie: String): Float {
        val cal = Calendar.getInstance()
        cal.add(Calendar.MONTH, -1)
        val moisAnnee = SimpleDateFormat("MM/yyyy", Locale.FRANCE).format(cal.time)
        val db = this.readableDatabase
        val cursor = db.rawQuery(
            "SELECT SUM(montant) FROM depenses WHERE categorie = ? AND strftime('%m/%Y', date) = ?",
            arrayOf(categorie, moisAnnee)
        )
        val total = if (cursor.moveToFirst() && !cursor.isNull(0)) cursor.getFloat(0) else 0f
        cursor.close()
        return total
    }
}