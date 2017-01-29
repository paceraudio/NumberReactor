package com.pacerdevelopment.numberreactor.app.vp_stats;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.pacerdevelopment.numberreactor.app.R;
import com.pacerdevelopment.numberreactor.app.model.GameStats;

import java.util.ArrayList;

/**
 * Created by jeffwconaway on 12/11/14.
 */
public class ViewStatsListAdapter extends BaseAdapter {


    private Context mContext;
    private ArrayList<GameStats> mStatsList = new ArrayList<GameStats>();


    public ViewStatsListAdapter(Context context, ArrayList<GameStats> list) {
        this.mContext = context;
        this.mStatsList = list;
    }


    @Override
    public int getCount() {
        return mStatsList.size();
    }

    @Override
    public Object getItem(int i) {
        return mStatsList.get(i);
    }

    @Override
    public long getItemId(int pos) {
        return pos;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        final GameStats gameStats = (GameStats) getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.game_stats_list_item, parent, false);
        }

        TextView gameNumberTV = (TextView) convertView.findViewById(R.id.list_t_v_game_number);
        gameNumberTV.setText(Integer.toString(gameStats.getGameNumber()));

        TextView gameDateTV = (TextView) convertView.findViewById(R.id.list_t_v_game_date);
        gameDateTV.setText(mContext.getString(R.string.date) + gameStats.getGameDate());

        TextView gameLevelTV = (TextView) convertView.findViewById(R.id.list_t_v_game_level);
        gameLevelTV.setText(mContext.getString(R.string.level) + Integer.toString(gameStats.getGameLevelReached()));

        TextView gameScoreTV = (TextView) convertView.findViewById(R.id.list_t_v_game_score);
        gameScoreTV.setText(mContext.getString(R.string.score) + Integer.toString(gameStats.getGamePointsScored()));
        return convertView;
    }
}
