/**
 * Copyright 2008 Joe LaPenna
 */

package com.joelapenna.foursquared.widget;

import com.joelapenna.foursquare.types.User;
import com.joelapenna.foursquared.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * @date February 15, 2010
 * @author Mark Wyszomierski (markww@gmail.com), foursquare.
 */
public class FriendRequestsAdapter extends BaseGroupAdapter<User> {

    private LayoutInflater mInflater;
    private int mLayoutToInflate;
    private ButtonRowClickHandler mClickListener;

    public FriendRequestsAdapter(Context context, ButtonRowClickHandler clickListener) {
        super(context);
        mInflater = LayoutInflater.from(context);
        mLayoutToInflate = R.layout.friend_request_list_item;
        mClickListener = clickListener;
    }

    public FriendRequestsAdapter(Context context, int layoutResource) {
        super(context);
        mInflater = LayoutInflater.from(context);
        mLayoutToInflate = layoutResource;
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
            convertView = mInflater.inflate(mLayoutToInflate, null);

            // Creates a ViewHolder and store references to the two children
            // views we want to bind data to.
            holder = new ViewHolder();
            holder.main = (LinearLayout) convertView.findViewById(R.id.addFriendListItemBackground);
            holder.name = (TextView) convertView.findViewById(R.id.addFriendListItemName);
            holder.info = (Button) convertView.findViewById(R.id.friendRequestInfoButton);
            holder.approve = (Button) convertView.findViewById(R.id.friendRequestApproveButton);
            holder.deny = (Button) convertView.findViewById(R.id.friendRequestDenyButton);

            convertView.setTag(holder);

            holder.info.setOnClickListener(mOnClickListenerInfo);
            holder.approve.setOnClickListener(mOnClickListenerApprove);
            holder.deny.setOnClickListener(mOnClickListenerDeny);
        } else {
            // Get the ViewHolder back to get fast access to the TextView
            // and the ImageView.
            holder = (ViewHolder) convertView.getTag();
        }

        User user = (User) getItem(position);
        holder.name.setText(user.getFirstname() + " "
                + (user.getLastname() != null ? user.getLastname() : ""));
        holder.info.setTag(new Integer(position));
        holder.approve.setTag(new Integer(position));
        holder.deny.setTag(new Integer(position));

        return convertView;
    }

    private OnClickListener mOnClickListenerInfo = new OnClickListener() {
        @Override
        public void onClick(View v) {
            Integer position = (Integer) v.getTag();
            mClickListener.onBtnClickInfo((User) getItem(position));
        }
    };

    private OnClickListener mOnClickListenerApprove = new OnClickListener() {
        @Override
        public void onClick(View v) {
            Integer position = (Integer) v.getTag();
            mClickListener.onBtnClickApprove((User) getItem(position));
        }
    };

    private OnClickListener mOnClickListenerDeny = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mClickListener != null) {
                Integer position = (Integer) v.getTag();
                mClickListener.onBtnClickDeny((User) getItem(position));
            }
        }
    };

    static class ViewHolder {
        LinearLayout main;
        TextView name;
        Button info;
        Button approve;
        Button deny;
    }

    public interface ButtonRowClickHandler {
        public void onBtnClickInfo(User user);

        public void onBtnClickApprove(User user);

        public void onBtnClickDeny(User user);
    }
}
