package app.quranhub.ui.mushaf.adapter;

import android.animation.ObjectAnimator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.thoughtbot.expandablerecyclerview.ExpandableRecyclerViewAdapter;
import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup;
import com.thoughtbot.expandablerecyclerview.viewholders.ChildViewHolder;
import com.thoughtbot.expandablerecyclerview.viewholders.GroupViewHolder;

import java.util.List;

import app.quranhub.R;
import app.quranhub.ui.mushaf.listener.ItemSelectionListener;
import app.quranhub.ui.mushaf.model.TopicCategory;
import app.quranhub.ui.mushaf.model.TopicModel;
import butterknife.BindView;
import butterknife.ButterKnife;

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
        holder.categoryTv.setText(category.getCategoryName());
        holder.itemView.setOnClickListener(v -> listener.onSelectItem(category));
    }

    @Override
    public void onBindGroupViewHolder(TopicViewHolder holder, int flatPosition
            , ExpandableGroup group) {
        TopicModel topicModel = ((TopicModel) group);
        holder.topicTv.setText(topicModel.getTopicName());

    }

    public static class CategoryViewHolder extends ChildViewHolder {

        @BindView(R.id.category_tv)
        TextView categoryTv;
        @BindView(R.id.separator)
        View seperator;

        public CategoryViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    public static class TopicViewHolder extends GroupViewHolder {
        @BindView(R.id.topic_tv)
        TextView topicTv;
        @BindView(R.id.arrow_iv)
        ImageView arrowIv;

        public TopicViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
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
            ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(arrowIv, "rotation", from, to);
            objectAnimator.setDuration(350);
            return objectAnimator;
        }
    }

}
