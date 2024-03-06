package nirmal.baby.capstoneproject.AdapterClass

import android.media.Image
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import nirmal.baby.capstoneproject.Data.OnBoardingItem
import nirmal.baby.capstoneproject.R

class OnBoardingItemsAdapter(private val onBoardingItems: List<OnBoardingItem>) :
    RecyclerView.Adapter<OnBoardingItemsAdapter.OnBoardingItemViewHolder>(){

        inner class OnBoardingItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            private val imageOnBoarding = view.findViewById<ImageView>(R.id.imageOnBoarding)
            private val textTitleOnBoarding = view.findViewById<TextView>(R.id.textOnBoardTitle)
            private val textDescriptionOnBoarding = view.findViewById<TextView>(R.id.textOnBoardDesc)


            fun bind(onBoardingItem: OnBoardingItem) {
                imageOnBoarding.setImageResource(onBoardingItem.onBoardingImage)
                textTitleOnBoarding.text = onBoardingItem.title
                textDescriptionOnBoarding.text = onBoardingItem.description
            }
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OnBoardingItemViewHolder {
        return OnBoardingItemViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.onboarding_item_container,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return onBoardingItems.size
    }

    override fun onBindViewHolder(holder: OnBoardingItemViewHolder, position: Int) {
        holder.bind(onBoardingItems[position])
    }
}