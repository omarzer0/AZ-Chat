package az.zero.azchat.presentation.auth.adapter.country_code

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import az.zero.azchat.domain.models.country_code.CountryCode
import az.zero.azchat.databinding.ItemCountryCodeBinding

class CountryCodeAdapter : RecyclerView.Adapter<CountryCodeAdapter.CountryCodeViewHolder>() {

    private var items: List<CountryCode> = ArrayList()

    @SuppressLint("NotifyDataSetChanged")
    fun changeItems(newItems: List<CountryCode>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CountryCodeViewHolder {
        val binding = ItemCountryCodeBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return CountryCodeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CountryCodeViewHolder, position: Int) {
        val currentItem = items[position]
        holder.bind(currentItem)
    }

    override fun getItemCount(): Int = items.size

    inner class CountryCodeViewHolder(private val binding: ItemCountryCodeBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    val clickedItem = items[adapterPosition]
                    onCountryCodeItemClickListener?.let { it(clickedItem) }
                }
            }
        }

        fun bind(currentItem: CountryCode) {
            binding.apply {
                countryNameTv.text = currentItem.name
                countryNumberTv.text = "+${currentItem.callingCode}"
            }
        }

    }

    private var onCountryCodeItemClickListener: ((CountryCode) -> Unit)? = null
    fun setOnCountryCodeItemClickListener(listener: (CountryCode) -> Unit) {
        onCountryCodeItemClickListener = listener
    }
}