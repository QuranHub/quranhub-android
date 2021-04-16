package app.quranhub.ui.mushaf.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import app.quranhub.R;
import app.quranhub.ui.mushaf.data.entity.Note;
import app.quranhub.ui.mushaf.model.DisplayedNote;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.ViewHolder> {

    private List<DisplayedNote> noteList;
    private List<DisplayedNote> filteredNoteList;
    private String[] noteTypes, suraText;
    private Context context;
    private NoteCallback listener;
    private boolean isEditable;

    public NotesAdapter(Context context, NoteCallback listener) {
        noteList = new ArrayList<>();
        filteredNoteList = new ArrayList<>();
        this.context = context;
        this.listener = listener;
        noteTypes = new String[]{
                context.getString(R.string.general_comment),
                context.getString(R.string.momerize_mistake),
                context.getString(R.string.tajweed_mistake)
        };
        suraText = context.getResources().getStringArray(R.array.sura_name);
        isEditable = false;
    }

    public void setEditable(boolean editable) {
        isEditable = editable;
        notifyDataSetChanged();
    }

    public void setNoteList(List<DisplayedNote> noteList) {
        this.noteList = noteList;
        this.filteredNoteList = noteList;
        notifyDataSetChanged();
    }


    public void filter(String inputQuery) {
        if (inputQuery.isEmpty()) {
            filteredNoteList = noteList;
        } else {
            List<DisplayedNote> filteredList = new ArrayList<>();
            for (DisplayedNote row : noteList) {
                if (row.getPure_text().toLowerCase().contains(inputQuery.toLowerCase())) {
                    filteredList.add(row);
                }
            }
            filteredNoteList = filteredList;
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.note_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DisplayedNote note = filteredNoteList.get(position);
        holder.ayaNumTv.setText(context.getString(R.string.ayas_num, String.valueOf(note.getSura_aya())));
        holder.ayaTv.setText(note.getText());
        holder.noteTypeTv.setText(noteTypes[note.getNoteType()]);
        holder.suraTv.setText(suraText[note.getSura() - 1]);
        //holder.ayaTv.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        //holder.detailsIv.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        if (isEditable) {
            holder.deleteIv.setVisibility(View.VISIBLE);
            holder.detailsIv.setVisibility(View.INVISIBLE);
        } else {
            holder.deleteIv.setVisibility(View.GONE);
            holder.detailsIv.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return filteredNoteList.size();
    }

    public void updateNoteType(Note note) {
        for (DisplayedNote displayedNote : filteredNoteList) {
            if (displayedNote.getAyaId() == note.getAyaId()) {
                displayedNote.setNoteType(note.getNoteType());
                displayedNote.setNoteRecorderPath(note.getNoteRecorderPath());
                displayedNote.setNoteText(note.getNoteText());
                notifyDataSetChanged();
                break;
            }
        }
    }

    public void setAllNotes() {
        filteredNoteList = noteList;
        notifyDataSetChanged();
    }

    public void setFilteredNotes(int noteType) {
        List<DisplayedNote> filteredList = new ArrayList<>();
        for (DisplayedNote displayedNote : noteList) {
            if (displayedNote.getNoteType() == noteType) {
                filteredList.add(displayedNote);
            }
        }
        filteredNoteList = filteredList;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.aya_tv)
        TextView ayaTv;
        @BindView(R.id.aya_num_tv)
        TextView ayaNumTv;
        @BindView(R.id.note_type_tv)
        TextView noteTypeTv;
        @BindView(R.id.tv_sura_name)
        TextView suraTv;
        @BindView(R.id.details_iv)
        ImageView detailsIv;
        @BindView(R.id.delete_iv)
        ImageView deleteIv;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        @OnClick(R.id.details_iv)
        public void onClickNoteDetails() {
            listener.onGetNoteDetails(filteredNoteList.get(getAdapterPosition()));
        }

        @OnClick(R.id.delete_iv)
        public void onClickDeleteNote() {
            listener.onDeleteNote(filteredNoteList.get(getAdapterPosition()).getAyaId());
            DisplayedNote removedNote = filteredNoteList.get(getAdapterPosition());
            filteredNoteList.remove(getAdapterPosition());
            notifyItemRemoved(getAdapterPosition());
            for (DisplayedNote note : noteList) {
                if (note.getAyaId() == removedNote.getAyaId()) {
                    noteList.remove(note);
                    break;
                }
            }
        }

        @OnClick(R.id.aya_tv)
        public void onNavigateToAya() {
            listener.onNavigateToAya(filteredNoteList.get(getAdapterPosition()).getAyaId(), filteredNoteList.get(getAdapterPosition()).getPage());
        }

    }

    public interface NoteCallback {
        void onNavigateToAya(int ayaId, int pageNum);

        void onGetNoteDetails(DisplayedNote note);

        void onDeleteNote(int ayaId);
    }
}
