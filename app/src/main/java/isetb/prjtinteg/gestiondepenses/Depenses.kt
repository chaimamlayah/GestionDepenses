package isetb.prjtinteg.gestiondepenses


data class Depenses(
    val id: Int = 0,
    val categorie: String,
    val description: String = "",
    val montant: Float,
    val date: String
) {

    constructor(categorie: String, description: String, montant: Float, date: String)
            : this(0, categorie, description, montant, date)


    constructor(categorie: String, montant: Float, date: String)
            : this(0, categorie, "", montant, date)


    override fun toString(): String {
        return "$categorie - $montant DT le $date"
    }
}