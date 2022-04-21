package az.zero.azchat.presentation.main.adapter.simple_info

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import az.zero.azchat.common.setImageUsingGlide
import az.zero.azchat.databinding.ItemSimpleInfoBinding
import az.zero.azchat.domain.models.simple_info.SimpleInfo

class SimpleInfoAdapter(
    val shouldShowLinks: Boolean = false,
    val onSimpleInfoClick: (SimpleInfo) -> Unit
) : RecyclerView.Adapter<SimpleInfoAdapter.SimpleInfoViewHolder>() {
    private var items: List<SimpleInfo> = ArrayList()

    @SuppressLint("NotifyDataSetChanged")
    fun changeItems(newItems: List<SimpleInfo>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SimpleInfoViewHolder {
        val binding = ItemSimpleInfoBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return SimpleInfoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SimpleInfoViewHolder, position: Int) {
        val currentItem = items[position]
        holder.bind(currentItem)
    }

    override fun getItemCount(): Int = items.size

    inner class SimpleInfoViewHolder(private val binding: ItemSimpleInfoBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    onSimpleInfoClick(items[adapterPosition])
                }
            }
        }

        fun bind(currentItem: SimpleInfo) {
            binding.apply {
                tvName.apply {
                    text = currentItem.name
                    isVisible = currentItem.name.isNotEmpty()
                }

                ivIcon.apply {
                    setImageUsingGlide(this, currentItem.icon)
                    isVisible = currentItem.icon.isNotEmpty()
                }


                tvLink.apply {
                    isVisible = shouldShowLinks
                    text = currentItem.link
                }

            }
        }

    }
}