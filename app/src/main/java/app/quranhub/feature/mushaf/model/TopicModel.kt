package app.quranhub.feature.mushaf.model

import app.quranhub.core.data.model.TopicCategory
import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup

data class TopicModel(
    var topicName: String,
    var topicCategories: List<TopicCategory>
) :
    ExpandableGroup<TopicCategory?>(topicName, topicCategories) {

    var isExpandable = false
}