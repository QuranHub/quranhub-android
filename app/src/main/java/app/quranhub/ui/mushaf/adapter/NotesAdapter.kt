package app.quranhub.ui.mushaf.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import app.quranhub.R
import app.quranhub.data.local.entity.Note
import app.quranhub.databinding.NoteItemBinding
import app.quranhub.ui.mushaf.model.DisplayedNote
import java.util.Locale

class NotesAdapter(context: Context, listener: NoteCallback) :
    RecyclerView.Adapter<NotesAdapter.ViewHolder?>() {

    private var noteList: MutableList<DisplayedNote>
    private var filteredNoteList: MutableList<DisplayedNote>

    private val noteTypes: Array<String>
    private val suraText: Array<String>
    private val context: Context
    private val listener: NoteCallback
    private var isEditable: Boolean

    init {
        noteList = ArrayList()
        filteredNoteList = ArrayList()
        this.context = context
        this.listener = listener
        noteTypes = arrayOf(
            context.getString(R.string.general_comment),
            context.getString(R.string.momerize_mistake),
            context.getString(R.string.tajweed_mistake)
        )
        suraText = context.resources.getStringArray(R.array.sura_name)
        isEditable = false
    }

    fun setEditable(editable: Boolean) {
        isEditable = editable
        notifyDataSetChanged()
    }

    fun setNoteList(noteList: MutableList<DisplayedNote>) {
        this.noteList = noteList
        filteredNoteList = noteList
        notifyDataSetChanged()
    }

    fun filter(inputQuery: String) {
        filteredNoteList = if (inputQuery.isEmpty()) {
            noteList
        } else {
            val filteredList: MutableList<DisplayedNote> = ArrayList()
            for (row in noteList) {
                if (row.pure_text.lowercase(Locale.getDefault())
                        .contains(inputQuery.lowercase(Locale.getDefault()))
                ) {
                    filteredList.add(row)
                }
            }
            filteredList
        }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.note_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val note = filteredNoteList[position]
        holder.binding.ayaNumTv.text =
            context.getString(R.string.ayas_num, note.sura_aya.toString())
        holder.binding.ayaTv.text = note.text
        holder.binding.noteTypeTv.text = noteTypes[note.noteType]
        holder.binding.tvSuraName.text = suraText[note.sura - 1]
        //holder.ayaTv.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        //holder.detailsIv.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        if (isEditable) {
            holder.binding.deleteIv.visibility = View.VISIBLE
            holder.binding.detailsIv.visibility = View.INVISIBLE
        } else {
            holder.binding.deleteIv.visibility = View.GONE
            holder.binding.detailsIv.visibility = View.VISIBLE
        }
    }

    override fun getItemCount(): Int {
        return filteredNoteList.size
    }

    fun updateNoteType(note: Note) {
        for (displayedNote in filteredNoteList) {
            if (displayedNote.ayaId == note.ayaId) {
                displayedNote.noteType = note.noteType
                displayedNote.noteRecorderPath = note.noteRecorderPath
                displayedNote.noteText = note.noteText
                notifyDataSetChanged()
                break
            }
        }
    }

    fun setAllNotes() {
        filteredNoteList = noteList
        notifyDataSetChanged()
    }

    fun setFilteredNotes(noteType: Int) {
        val filteredList: MutableList<DisplayedNote> = ArrayList()
        for (displayedNote in noteList) {
            if (displayedNote.noteType == noteType) {
                filteredList.add(displayedNote)
            }
        }
        filteredNoteList = filteredList
        notifyDataSetChanged()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var binding: NoteItemBinding

        init {
            binding = NoteItemBinding.bind(itemView)
            attachListeners()
        }

        private fun attachListeners() {
            binding.detailsIv.setOnClickListener { v: View? -> onClickNoteDetails() }
            binding.deleteIv.setOnClickListener { v: View? -> onClickDeleteNote() }
            binding.ayaTv.setOnClickListener { v: View? -> onNavigateToAya() }
        }

        private fun onClickNoteDetails() {
            listener.onGetNoteDetails(filteredNoteList[adapterPosition])
        }

        private fun onClickDeleteNote() {
            listener.onDeleteNote(filteredNoteList[adapterPosition].ayaId)
            val removedNote = filteredNoteList[adapterPosition]
            filteredNoteList.removeAt(adapterPosition)
            notifyItemRemoved(adapterPosition)
            for (note in noteList) {
                if (note.ayaId == removedNote.ayaId) {
                    noteList.remove(note)
                    break
                }
            }
        }

        private fun onNavigateToAya() {
            listener.onNavigateToAya(
                filteredNoteList[adapterPosition].ayaId,
                filteredNoteList[adapterPosition].page
            )
        }
    }

    interface NoteCallback {
        fun onNavigateToAya(ayaId: Int, pageNum: Int)
        fun onGetNoteDetails(note: DisplayedNote?)
        fun onDeleteNote(ayaId: Int)
    }
}