/**
 * Copyright 2010 Mark Wyszomierski
 */

package com.joelapenna.foursquared.widget;

import com.joelapenna.foursquared.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * This is a custom adapter which has two different cell types.
 * 
 * @date March 24, 2010
 * @author Mark Wyszomierski (markww@gmail.com)
 * 
 */
public class TipActivityAdapter extends BaseTipAdapter {
    
    public static final int ACTION_ID_NONE          = 0;
    public static final int ACTION_ID_ADD_TODO_LIST = 1;
    public static final int ACTION_ID_IVE_DONE_THIS = 2;

    private LayoutInflater mInflater;
    private int mLayoutToInflate0;
    private int mLayoutToInflate1;
    private String mVenueName;
    private String mTipText;
    private Context mContext;

    
    public TipActivityAdapter(Context context, String venueName, String tipText) {
        super(context);
        mContext = context;
        mInflater = LayoutInflater.from(context);
        mLayoutToInflate0 = R.layout.tip_activity_desc_list_item;
        mLayoutToInflate1 = R.layout.tip_activity_action_list_item;
        mVenueName = venueName;
        mTipText = tipText;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        switch (position) {
            case ACTION_ID_NONE:
                return generateView0(convertView);
            case ACTION_ID_ADD_TODO_LIST:
            case ACTION_ID_IVE_DONE_THIS:
                return generateViewActions(convertView, position);
        }
        
        return convertView;
    }
    
    private View generateView0(View convertView) {
        if (convertView == null) {
            convertView = mInflater.inflate(mLayoutToInflate0, null);
        } else {
            Integer id = (Integer)convertView.getTag();
            if (id.intValue() != ACTION_ID_NONE) {
                convertView = mInflater.inflate(mLayoutToInflate0, null);
            }
        }

        TextView tv1 = (TextView) convertView.findViewById(R.id.tipActivityDescListItemLabel1);
        TextView tv2 = (TextView) convertView.findViewById(R.id.tipActivityDescListItemLabel2);
        tv1.setText(mVenueName);
        tv2.setText(mTipText);
        
        convertView.setTag(new Integer(ACTION_ID_NONE));
        return convertView;
    }
    
    private View generateViewActions(View convertView, int position) {
        if (convertView == null) {
            convertView = mInflater.inflate(mLayoutToInflate1, null);
        } else {
            Integer id = (Integer)convertView.getTag();
            if (id.intValue() != ACTION_ID_ADD_TODO_LIST && 
                id.intValue() != ACTION_ID_IVE_DONE_THIS) {
                convertView = mInflater.inflate(mLayoutToInflate1, null);
            }
        }
        
        ImageView iv = (ImageView) convertView.findViewById(R.id.tipActivityActionListItemIcon);
        TextView tv = (TextView) convertView.findViewById(R.id.tipActivityActionListItemLabel);
        switch (position) {
            case ACTION_ID_ADD_TODO_LIST:
                iv.setImageResource(R.drawable.user_action_add_friend);
                tv.setText(mContext.getResources().getString(R.string.tip_activity_action_todo));
                break;
            case ACTION_ID_IVE_DONE_THIS:
                iv.setImageResource(R.drawable.user_action_friend_pending);
                tv.setText(mContext.getResources().getString(R.string.tip_activity_action_done_this));
                break;
        }

        convertView.setTag(new Integer(position));
        return convertView;
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
    
    @Override
    public boolean isEnabled(int position) {
        return position > ACTION_ID_NONE;
    }
    
    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }
}
