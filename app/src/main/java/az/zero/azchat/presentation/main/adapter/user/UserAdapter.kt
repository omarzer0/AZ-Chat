package az.zero.azchat.presentation.main.adapter.user

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import az.zero.azchat.common.setImageUsingGlide
import az.zero.azchat.databinding.ItemUserBinding
import az.zero.azchat.domain.models.user.User

class UserAdapter(
    val onUserChosenToJoinGroup: (MutableList<String>) -> Unit,
    val onUserClickListener: (User) -> Unit,
    val onImageClick: (image: String) -> Unit,
    val onDeleteUserClick: ((id: String) -> Unit)? = null
) : ListAdapter<User, UserAdapter.UserViewHolder>(DiffUtil) {


    private var selectedUsers = mutableListOf<String>()
    private var selectionModeIsON = false
    private var isDeleteModeOn = false

    fun setSelectedMode(isOn: Boolean) {
        selectionModeIsON = isOn
    }

    fun setDeleteMode(isOn: Boolean) {
        isDeleteModeOn = isOn
    }

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
                if (selectionModeIsON) {
                    addUserToGroup(adapterPosition)
                } else {
                    goToChat(adapterPosition)
                }
            }

            binding.ivRemoveUser.setOnClickListener {
                if (adapterPosition == RecyclerView.NO_POSITION) return@setOnClickListener
                onDeleteUserClick?.invoke(getItem(adapterPosition).uid ?: "")
            }

            binding.userImageIv.setOnClickListener {
                if (adapterPosition == RecyclerView.NO_POSITION) return@setOnClickListener
                onImageClick(getItem(adapterPosition).imageUrl ?: "")
            }
        }

        fun bind(currentItem: User) {
            binding.apply {
                setImageUsingGlide(userImageIv, currentItem.imageUrl)
                userNameTv.text = currentItem.name
                userBioTv.text = if (currentItem.bio!!.trim().isEmpty())
                    "Lazy user didn't write anything!" else currentItem.bio

                if (selectionModeIsON) {
                    val uid = currentItem.uid!!
                    selectedIv.isVisible = selectedUsers.any { it == uid }
                }

                ivRemoveUser.isVisible = isDeleteModeOn
            }
        }
    }

    private fun addUserToGroup(adapterPosition: Int) {
        val userID = getItem(adapterPosition).uid!!
        if (selectedUsers.any { it == userID }) selectedUsers.remove(userID)
        else selectedUsers.add(userID)
        notifyItemChanged(adapterPosition)
        onUserChosenToJoinGroup(selectedUsers)
    }

    private fun goToChat(adapterPosition: Int) {
        onUserClickListener(getItem(adapterPosition))
    }

    fun updateSelectedUsers(selectedUsersList: MutableList<String>) {
        selectedUsers.clear()
        selectedUsers.addAll(selectedUsersList)
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