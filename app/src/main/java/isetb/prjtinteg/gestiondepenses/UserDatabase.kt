package isetb.prjtinteg.gestiondepenses

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class UserDatabase(
    context: Context?
) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object{
        const val DATABASE_VERSION: Int = 1
        const val DATABASE_NAME: String = "user_db"
        const val TUser = "user"
        const val CID = "id"
        const val CNP = "nom_prenom"

        const val CE = "email"
        const val CM = "mdp"
    }

    val CREATE_STUDENT_TABLE = ("CREATE TABLE "+ TUser + "("
            + CID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + CNP + " TEXT NOT NULL, "
            +CE +" TEXT UNIQUE, "
            +CM+" TEXT)")

    lateinit var db: SQLiteDatabase
    override fun onCreate(p0: SQLiteDatabase) {
        p0.execSQL(CREATE_STUDENT_TABLE)
    }

    override fun onUpgrade(
        p0: SQLiteDatabase,
        p1: Int,
        p2: Int
    ) {
        p0.execSQL("DROP TABLE "+TUser+";")
        onCreate(p0)
    }

    fun addUser(u: User): Boolean{
        db=writableDatabase
        val values= ContentValues().apply {
            put(CNP,u.nom_prenom)
            put(CE,u.email)
            put(CM,u.mdp)
        }
        val x=db.insert(TUser,null,values)

        return (x!=-1L)
    }

    fun updateUser(u: User):Int{
        db=writableDatabase
        val values= ContentValues().apply {
            put(CNP,u.nom_prenom)
            put(CE,u.email)
            put(CM,u.mdp)
        }
        val res=db.update(TUser,values,CID+"="+u.id,null)
        return res
    }

    fun removeUser(id: Int): Boolean{
        db=writableDatabase
        val res = db.delete(TUser, "$CID = ?", arrayOf(id.toString()))

        return (res>0)
    }

    fun checkUser(email: String, mdp: String): User? {
        db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TUser WHERE $CE=? AND $CM=?", arrayOf(email, mdp))
        var user: User? = null
        if (cursor.moveToFirst()) {
            user = User(cursor.getInt(cursor.getColumnIndexOrThrow(CID)),
                cursor.getString(cursor.getColumnIndexOrThrow(CNP)),
                cursor.getString(cursor.getColumnIndexOrThrow(CE)),
                cursor.getString(cursor.getColumnIndexOrThrow(CM))
            )
        }
        cursor.close()

        return user
    }
}