
package com.joelapenna.foursquared.widget;

import java.io.IOException;
import java.util.Observable;
import java.util.Observer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.joelapenna.foursquare.types.Group;
import com.joelapenna.foursquare.types.Score;
import com.joelapenna.foursquared.FoursquaredSettings;
import com.joelapenna.foursquared.R;
import com.joelapenna.foursquared.util.RemoteResourceManager;

public class ScoreListAdapter extends BaseGroupAdapter<Score> implements ObservableAdapter {
    private static final String TAG = "ScoreListAdapter";
    private static final boolean DEBUG = FoursquaredSettings.DEBUG;
    private static final String PLUS = " +";
    private RemoteResourceManager mRrm;
    private RemoteResourceManagerObserver mResourcesObserver;
    private Handler mHandler = new Handler();

    private LayoutInflater mInflater;

    public ScoreListAdapter(Context context, RemoteResourceManager rrm) {
        super(context);
        mRrm = rrm;
        mResourcesObserver = new RemoteResourceManagerObserver();
        mInflater = LayoutInflater.from(context);

        mRrm.addObserver(mResourcesObserver);
    }
    
    public void removeObserver() {
        mRrm.deleteObserver(mResourcesObserver);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // A ViewHolder keeps references to children views to avoid unnecessary
        // calls to findViewById() on each row.
        ViewHolder holder;

        // When convertView is not null, we can reuse it directly, there is no
        // need to re-inflate it. We only inflate a new View when the
        // convertView supplied by ListView is null.
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.score_list_item, null);

            // Creates a ViewHolder and store references to the two children
            // views we want to bind data to.
            holder = new ViewHolder();
            holder.scoreIcon = (ImageView) convertView.findViewById(R.id.scoreIcon);
            holder.scoreDesc = (TextView) convertView.findViewById(R.id.scoreDesc);
            holder.scoreNum = (TextView) convertView.findViewById(R.id.scoreNum);
            convertView.setTag(holder);
        } else {
            // Get the ViewHolder back to get fast access to the TextView
            // and the ImageView.
            holder = (ViewHolder) convertView.getTag();
        }

        Score score = (Score) getItem(position);
        holder.scoreDesc.setText(score.getMessage());

        String scoreIconUrl = score.getIcon();
        if (!TextUtils.isEmpty(scoreIconUrl)) {
            try {
                Bitmap bitmap = BitmapFactory.decodeStream(//
                        mRrm.getInputStream(Uri.parse(score.getIcon())));
                holder.scoreIcon.setImageBitmap(bitmap);
            } catch (IOException e) {
                if (DEBUG) Log.d(TAG, "Could not load bitmap. We don't have it yet.");
                holder.scoreIcon.setImageResource(R.drawable.default_on);
            }
            holder.scoreIcon.setVisibility(View.VISIBLE);
            holder.scoreNum.setText(PLUS + score.getPoints());
        } else {
            holder.scoreIcon.setVisibility(View.INVISIBLE);
            holder.scoreNum.setText(score.getPoints());
        }

        return convertView;
    }

    static class ViewHolder {
        ImageView scoreIcon;
        TextView scoreDesc;
        TextView scoreNum;
    }

    @Override
    public void setGroup(Group<Score> g) {
        super.setGroup(g);
        for (int i = 0; i < group.size(); i++) {
            Uri iconUri = Uri.parse((group.get(i)).getIcon());
            if (!mRrm.exists(iconUri)) {
                mRrm.request(iconUri);
            }
        }
    }

    private class RemoteResourceManagerObserver implements Observer {
        @Override
        public void update(Observable observable, Object data) {
            if (DEBUG) Log.d(TAG, "Fetcher got: " + data);
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    notifyDataSetChanged();
                }
            });
        }
    }
}
