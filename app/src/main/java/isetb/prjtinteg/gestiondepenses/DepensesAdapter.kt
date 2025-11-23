package isetb.prjtinteg.gestiondepenses

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

class DepensesAdapter :
    ListAdapter<Depenses, DepensesAdapter.ViewHolder>(DepenseDiffCallback()) {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvCategorie: TextView = itemView.findViewById(R.id.tvCategorie)
        val tvDescription: TextView = itemView.findViewById(R.id.tvDescription)
        val tvMontant: TextView = itemView.findViewById(R.id.tvMontant)
        val tvDate: TextView = itemView.findViewById(R.id.tvDate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_depense, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val depense = getItem(position)

        holder.tvCategorie.text = depense.categorie.uppercase()
        holder.tvDescription.text = if (depense.description.isBlank()) "— Aucune description —"
        else depense.description

        holder.tvMontant.text = String.format("%.2f DT", depense.montant)
        holder.tvMontant.setTextColor(
            if (depense.montant > 100f)
                holder.itemView.context.getColor(android.R.color.holo_red_dark)
            else
                holder.itemView.context.getColor(android.R.color.holo_green_dark)
        )

        holder.tvDate.text = depense.date
    }

    private class DepenseDiffCallback : DiffUtil.ItemCallback<Depenses>() {
        override fun areItemsTheSame(old: Depenses, new: Depenses) = old.id == new.id
        override fun areContentsTheSame(old: Depenses, new: Depenses) = old == new
    }
}