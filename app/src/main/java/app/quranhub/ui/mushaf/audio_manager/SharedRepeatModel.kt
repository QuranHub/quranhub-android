package app.quranhub.ui.mushaf.audio_manager;

import app.quranhub.ui.mushaf.model.RepeatModel;

public class SharedRepeatModel {

    public static RepeatModel repeatModel;
    public static boolean isRepeatModelChanged;


    public static RepeatModel getRepeatModel() {
        return repeatModel;
    }

    public static void setRepeatModel(RepeatModel repeatModel) {
        SharedRepeatModel.repeatModel = repeatModel;
    }

    public static boolean isIsRepeatModelChanged() {
        return isRepeatModelChanged;
    }

    public static void setIsRepeatModelChanged(boolean isRepeatModelChanged) {
        SharedRepeatModel.isRepeatModelChanged = isRepeatModelChanged;
    }
}
