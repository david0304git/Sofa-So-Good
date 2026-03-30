package com.speed.sofasogood.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.speed.sofasogood.R;
import com.speed.sofasogood.models.LeaderboardEntry;

import java.util.ArrayList;
import java.util.List;

public class LeaderboardAdapter extends RecyclerView.Adapter<LeaderboardAdapter.ViewHolder>{
    private List<LeaderboardEntry> entryList = new ArrayList<>();

    public void setEntryList(List<LeaderboardEntry> entryList) {
        this.entryList = entryList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_leaderboard, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        LeaderboardEntry entry = entryList.get(position);

        holder.tvRank.setText("#" + entry.getRank());
        holder.tvPlayerName.setText(entry.getPlayerName());
        holder.tvGroupId.setText("Group: " + entry.getGroupId());
        holder.tvScore.setText("Score: " + entry.getScore());
    }

    @Override
    public int getItemCount() {
        return entryList == null ? 0 : entryList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvRank, tvPlayerName, tvGroupId, tvScore;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRank = itemView.findViewById(R.id.tvRank);
            tvPlayerName = itemView.findViewById(R.id.tvPlayerName);
            tvGroupId = itemView.findViewById(R.id.tvGroupId);
            tvScore = itemView.findViewById(R.id.tvScore);
        }
    }
}
