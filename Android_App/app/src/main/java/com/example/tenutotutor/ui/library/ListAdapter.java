package com.example.tenutotutor.ui.library;

import android.annotation.SuppressLint;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tenutotutor.MainActivity;
import com.example.tenutotutor.R;

import java.util.ArrayList;

public class ListAdapter extends RecyclerView.Adapter<ListAdapter.ViewHolder> {

    private final ArrayList<Midi> MidiList;
    private final onCLickListener onCLickListener;
    private final View.OnCreateContextMenuListener onCreateContextMenuListener;

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnCreateContextMenuListener {
        private final TextView songName;
        private final TextView duration;
        private final TextView numberOfPlayed;
        private final onCLickListener onCLickListener;
        private final View.OnCreateContextMenuListener onCreateContextMenuListener;

        public ViewHolder(View view, onCLickListener onCLickListener, View.OnCreateContextMenuListener onCreateContextMenuListener) {
            super(view);
            // Define click listener for the ViewHolder's View
            this.onCLickListener = onCLickListener;
            this.onCreateContextMenuListener = onCreateContextMenuListener;
            duration = view.findViewById(R.id.duration);
            numberOfPlayed = view.findViewById(R.id.number_of_played);
            songName = view.findViewById(R.id.song_name);
            view.setOnClickListener(this);
            view.setOnCreateContextMenuListener(this);
        }

        public TextView getSongName() {
            return songName;
        }

        public TextView getDuration() {
            return duration;
        }

        public TextView getNumberOfPlayed() {
            return numberOfPlayed;
        }

        @Override
        public void onClick(View v) {
            onCLickListener.onCLick(getAdapterPosition());
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            onCreateContextMenuListener.onCreateContextMenu(menu, v, menuInfo);
            LibraryFragment.setPos(getLayoutPosition());
        }

    }

    /**
     * Initialize the dataset of the Adapter.
     *
     * @param midiList list of midi files
     */
    public ListAdapter(ArrayList<Midi> midiList, onCLickListener onCLickListener, View.OnCreateContextMenuListener onCreateContextMenuListener) {
        if (midiList == null) {
            this.MidiList = new ArrayList<>();
        } else {
            this.MidiList = midiList;
        }
        this.onCLickListener = onCLickListener;
        this.onCreateContextMenuListener = onCreateContextMenuListener;
    }

    // Create new views (invoked by the layout manager)
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.text_row_item, viewGroup, false);
        return new ViewHolder(view, onCLickListener, onCreateContextMenuListener);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        viewHolder.getSongName().setText(MidiList.get(position).getSongName());
        viewHolder.getNumberOfPlayed().setText("Finished on part  " + MidiList.get(position).getNumberOfPlayed());
        viewHolder.getDuration().setText("Duration: " + MidiList.get(position).getDuration());

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return MidiList.size();
    }

    public interface onCLickListener {
        void onCLick(int position);
    }

    public interface onCreateContextMenuListener {
        void onCreateContextMenu(ContextMenu contextMenu, View view, ContextMenu.ContextMenuInfo contextMenuInfo);
    }

    public interface RecyclerViewClickListener {
        public void recyclerViewListClicked(View v, int position);
    }

}

