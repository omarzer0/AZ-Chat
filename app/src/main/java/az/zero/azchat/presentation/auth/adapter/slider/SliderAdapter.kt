package az.zero.azchat.presentation.auth.adapter.slider

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import az.zero.azchat.databinding.ItemSliderBinding
import az.zero.azchat.domain.models.slider.SliderItem

class SliderAdapter : RecyclerView.Adapter<SliderAdapter.SliderViewHolder>() {

    private var items: List<SliderItem> = ArrayList()

    @SuppressLint("NotifyDataSetChanged")
    fun changeItems(newItems: List<SliderItem>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SliderViewHolder {
        val binding = ItemSliderBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return SliderViewHolder(binding)
    }


    override fun onBindViewHolder(holder: SliderViewHolder, position: Int) {
        val currentItem = items[position]
        holder.bind(currentItem)
    }


    override fun getItemCount() = items.size

    inner class SliderViewHolder(private val binding: ItemSliderBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(currentItem: SliderItem) {
            binding.ivSlideImage.setAnimation(currentItem.animationRawId)
//            binding.ivSlideImage.setImageResource(currentItem.image)
            binding.tvHeader.text = currentItem.header
            binding.tvDescription.text = currentItem.description
        }
    }

}