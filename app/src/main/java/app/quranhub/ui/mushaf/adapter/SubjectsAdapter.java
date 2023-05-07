package app.quranhub.ui.mushaf.adapter;

import android.animation.ObjectAnimator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.thoughtbot.expandablerecyclerview.ExpandableRecyclerViewAdapter;
import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup;
import com.thoughtbot.expandablerecyclerview.viewholders.ChildViewHolder;
import com.thoughtbot.expandablerecyclerview.viewholders.GroupViewHolder;

import java.util.List;

import app.quranhub.R;
import app.quranhub.databinding.ItemTopicBinding;
import app.quranhub.databinding.ItemTopicCategoryBinding;
import app.quranhub.ui.mushaf.listener.ItemSelectionListener;
import app.quranhub.ui.mushaf.model.TopicCategory;
import app.quranhub.ui.mushaf.model.TopicModel;

public class SubjectsAdapter extends
        ExpandableRecyclerViewAdapter<SubjectsAdapter.TopicViewHolder, SubjectsAdapter.CategoryViewHolder> {

    private ItemSelectionListener<TopicCategory> listener;

    public SubjectsAdapter(List<? extends ExpandableGroup> groups
            , ItemSelectionListener<TopicCategory> listener) {
        super(groups);
        this.listener = listener;
    }

    @Override
    public TopicViewHolder onCreateGroupViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_topic_category, parent, false);
        return new TopicViewHolder(view);
    }

    @Override
    public CategoryViewHolder onCreateChildViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_topic, parent, false);
        return new CategoryViewHolder(view);
    }


    @Override
    public void onBindChildViewHolder(CategoryViewHolder holder, int flatPosition
            , ExpandableGroup group, int childIndex) {
        TopicCategory category = ((TopicModel) group).getItems().get(childIndex);
        holder.binding.categoryTv.setText(category.getCategoryName());
        holder.itemView.setOnClickListener(v -> listener.onSelectItem(category));
    }

    @Override
    public void onBindGroupViewHolder(TopicViewHolder holder, int flatPosition
            , ExpandableGroup group) {
        TopicModel topicModel = ((TopicModel) group);
        holder.binding.topicTv.setText(topicModel.getTopicName());

    }

    public static class CategoryViewHolder extends ChildViewHolder {

        ItemTopicBinding binding;

        public CategoryViewHolder(View itemView) {
            super(itemView);
            binding = ItemTopicBinding.bind(itemView);
        }
    }

    public static class TopicViewHolder extends GroupViewHolder {

        ItemTopicCategoryBinding binding;

        public TopicViewHolder(View itemView) {
            super(itemView);
            binding = ItemTopicCategoryBinding.bind(itemView);
        }

        @Override
        public void expand() {
            expandArrow();
        }

        @Override
        public void collapse() {
            collapseArrow();
        }

        private void expandArrow() {
            changeRotate(0f, 180f).start();
        }

        private void collapseArrow() {
            changeRotate(180f, 0f).start();
        }

        private ObjectAnimator changeRotate(float from, float to) {
            ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(binding.arrowIv, "rotation", from, to);
            objectAnimator.setDuration(350);
            return objectAnimator;
        }
    }

}
