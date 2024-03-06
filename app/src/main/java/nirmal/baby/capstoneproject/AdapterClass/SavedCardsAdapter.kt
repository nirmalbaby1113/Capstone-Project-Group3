package nirmal.baby.capstoneproject.AdapterClass

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import nirmal.baby.capstoneproject.Data.SavedCard
import nirmal.baby.capstoneproject.R

// Create a RecyclerView adapter
class SavedCardsAdapter(private val savedCards: List<SavedCard>) :
    RecyclerView.Adapter<SavedCardsAdapter.SavedCardViewHolder>() {

    class SavedCardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // ViewHolder setup
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SavedCardViewHolder {
        // Inflate your saved card item layout here
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_saved_card, parent, false)
        return SavedCardViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: SavedCardViewHolder, position: Int) {
        // Bind data to ViewHolder
        val currentCard = savedCards[position]
        // Set up your ViewHolder views with the data
    }

    override fun getItemCount(): Int {
        return savedCards.size
    }
}