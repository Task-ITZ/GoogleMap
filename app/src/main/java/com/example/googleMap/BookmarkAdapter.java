package com.example.googleMap;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class BookmarkAdapter extends RecyclerView.Adapter<BookmarkAdapter.ViewHolder> {

    private final List<Bookmark> bookmarkList;
    private IClickItemBookmarkListener iClickItemBookmarkListener;


    public BookmarkAdapter(List<Bookmark> bookmarkList, IClickItemBookmarkListener iClickItemBookmarkListener) {
        this.bookmarkList = bookmarkList;
        this.iClickItemBookmarkListener = iClickItemBookmarkListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_place, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Bookmark bookmark = bookmarkList.get(position);
        holder.title.setText(bookmark.getName());
        holder.layoutItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                iClickItemBookmarkListener.onClickItemBookmark(bookmark);
            }
        });
    }

    @Override
    public int getItemCount() {
        return bookmarkList.size();
    }


    public static class ViewHolder extends RecyclerView.ViewHolder{
        private final TextView title;
        private final LinearLayout layoutItem;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.place_name);
            layoutItem = itemView.findViewById(R.id.layout_item);
        }
    }
}
