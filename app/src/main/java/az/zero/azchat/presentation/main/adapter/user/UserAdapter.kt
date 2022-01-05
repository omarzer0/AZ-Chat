package az.zero.azchat.presentation.main.adapter.user

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import az.zero.azchat.common.setImageUsingGlide
import az.zero.azchat.data.models.user.User
import az.zero.azchat.databinding.ItemUserBinding

class UserAdapter :
    ListAdapter<User, UserAdapter.UserViewHolder>(DiffUtil) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding = ItemUserBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return UserViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val currentItem = getItem(position)
        if (currentItem != null) holder.bind(currentItem)
    }

    inner class UserViewHolder(private val binding: ItemUserBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                onStudentClickListener?.let {
                    it(getItem(adapterPosition))
                }
            }
        }

        fun bind(currentItem: User) {
            binding.apply {
                setImageUsingGlide(userImageIv, currentItem.imageUrl)
                userNameTv.text = currentItem.name
                userBioTv.text = currentItem.bio
            }
        }
    }

    private var onStudentClickListener: ((User) -> Unit)? = null
    fun setOnStudentClickListener(listener: (User) -> Unit) {
        onStudentClickListener = listener
    }


    companion object {
        private val DiffUtil = object : DiffUtil.ItemCallback<User>() {
            override fun areItemsTheSame(oldItem: User, newItem: User) =
                oldItem.uid == newItem.uid

            override fun areContentsTheSame(oldItem: User, newItem: User) =
                oldItem == newItem
        }
    }
}