package az.zero.azchat.presentation.main.adapter.group

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import az.zero.azchat.common.setImageUsingGlide
import az.zero.azchat.data.models.group.Group
import az.zero.azchat.databinding.ItemGroupBinding
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions

class GroupAdapter(options: FirestoreRecyclerOptions<Group>, val uid: String) :
    FirestoreRecyclerAdapter<Group, GroupAdapter.GroupViewHolder>(options) {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupViewHolder {
        val binding = ItemGroupBinding.inflate(
            LayoutInflater.from(parent.context),
            parent, false
        )
        return GroupViewHolder(binding)
    }

    override fun onBindViewHolder(viewHolder: GroupViewHolder, position: Int, group: Group) {
        viewHolder.bind(group)
    }

    inner class GroupViewHolder(private val binding: ItemGroupBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                onStudentClickListener?.let {
                    it(getItem(adapterPosition))
                }
            }
        }

        fun bind(group: Group) {
            if (group.hasNullField()) return
            binding.apply {
                groupNameTv.text = group.name
//                lastMessageTv.text = group.
                try {
                    val imageKey = group.imageMap!!.keys.filter { it != uid }[0]
                    val image = group.imageMap!![imageKey]
                    setImageUsingGlide(groupImageIv, image)
                }catch (e:Exception){

                }

            }
        }
    }


    private var onStudentClickListener: ((Group) -> Unit)? = null
    fun setOnStudentClickListener(listener: (Group) -> Unit) {
        onStudentClickListener = listener
    }


}