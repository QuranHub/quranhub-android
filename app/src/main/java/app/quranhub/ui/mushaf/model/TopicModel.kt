package app.quranhub.ui.mushaf.model;


import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup;

import java.util.List;

public class TopicModel extends ExpandableGroup<TopicCategory> {

    private String topicName;
    private boolean isExpandable;
    private List<TopicCategory> topicCategories;

    public TopicModel(String topicName, List<TopicCategory> topicCategories) {
        super(topicName, topicCategories);
        this.topicName = topicName;
        this.topicCategories = topicCategories;
        this.isExpandable = false;
    }

    public String getTopicName() {
        return topicName;
    }

    public boolean isExpandable() {
        return isExpandable;
    }

    public void setExpandable(boolean expandable) {
        isExpandable = expandable;
    }

    public void setTopicName(String topicName) {
        this.topicName = topicName;
    }

    public List<TopicCategory> getTopicCategories() {
        return topicCategories;
    }

    public void setTopicCategories(List<TopicCategory> topicCategories) {
        this.topicCategories = topicCategories;
    }


}
