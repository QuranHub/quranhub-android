package app.quranhub.ui.mushaf.adapter

import android.animation.ObjectAnimator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import app.quranhub.R
import app.quranhub.databinding.ItemTopicBinding
import app.quranhub.databinding.ItemTopicCategoryBinding
import app.quranhub.ui.mushaf.adapter.SubjectsAdapter.CategoryViewHolder
import app.quranhub.ui.mushaf.adapter.SubjectsAdapter.TopicViewHolder
import app.quranhub.ui.mushaf.listener.ItemSelectionListener
import app.quranhub.ui.mushaf.model.TopicCategory
import app.quranhub.ui.mushaf.model.TopicModel
import com.thoughtbot.expandablerecyclerview.ExpandableRecyclerViewAdapter
import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup
import com.thoughtbot.expandablerecyclerview.viewholders.ChildViewHolder
import com.thoughtbot.expandablerecyclerview.viewholders.GroupViewHolder

class SubjectsAdapter(
    groups: List<ExpandableGroup<*>?>?, private val listener: ItemSelectionListener<TopicCategory>
) : ExpandableRecyclerViewAdapter<TopicViewHolder, CategoryViewHolder>(groups) {

    override fun onCreateGroupViewHolder(parent: ViewGroup, viewType: Int): TopicViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_topic_category, parent, false)
        return TopicViewHolder(view)
    }

    override fun onCreateChildViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_topic, parent, false)
        return CategoryViewHolder(view)
    }

    override fun onBindChildViewHolder(
        holder: CategoryViewHolder, flatPosition: Int, group: ExpandableGroup<*>, childIndex: Int
    ) {
        val category = (group as TopicModel).items[childIndex]
        category?.let { c ->
            holder.binding.categoryTv.text = c.categoryName
            holder.itemView.setOnClickListener { listener.onSelectItem(c) }
        }
    }

    override fun onBindGroupViewHolder(
        holder: TopicViewHolder, flatPosition: Int, group: ExpandableGroup<*>
    ) {
        val topicModel = group as TopicModel
        holder.binding.topicTv.text = topicModel.topicName
    }

    class CategoryViewHolder(itemView: View?) : ChildViewHolder(itemView) {
        var binding: ItemTopicBinding

        init {
            binding = ItemTopicBinding.bind(itemView!!)
        }
    }

    class TopicViewHolder(itemView: View?) : GroupViewHolder(itemView) {
        var binding: ItemTopicCategoryBinding

        init {
            binding = ItemTopicCategoryBinding.bind(itemView!!)
        }

        override fun expand() {
            expandArrow()
        }

        override fun collapse() {
            collapseArrow()
        }

        private fun expandArrow() {
            changeRotate(0f, 180f).start()
        }

        private fun collapseArrow() {
            changeRotate(180f, 0f).start()
        }

        private fun changeRotate(from: Float, to: Float): ObjectAnimator {
            val objectAnimator = ObjectAnimator.ofFloat(binding.arrowIv, "rotation", from, to)
            objectAnimator.duration = 350
            return objectAnimator
        }
    }
}