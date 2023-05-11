package app.quranhub.ui.mushaf.model

import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup

data class TopicModel(
    var topicName: String,
    var topicCategories: List<TopicCategory>
) :
    ExpandableGroup<TopicCategory?>(topicName, topicCategories) {

    var isExpandable = false
}