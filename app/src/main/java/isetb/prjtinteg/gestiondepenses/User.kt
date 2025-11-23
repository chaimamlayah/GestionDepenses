package isetb.prjtinteg.gestiondepenses

class User(var id:Int, val nom_prenom: String, val email: String, val mdp: String) {

    constructor(nom_prenom: String, email: String, mdp: String):this(0,nom_prenom,email,mdp)
}