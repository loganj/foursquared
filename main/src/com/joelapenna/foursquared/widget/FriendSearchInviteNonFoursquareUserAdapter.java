/**
 * Copyright 2010 Mark Wyszomierski
 */

package com.joelapenna.foursquared.widget;

import com.joelapenna.foursquared.R;
import com.joelapenna.foursquared.util.AddressBookEmailBuilder.ContactSimple;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * @date April 26, 2010
 * @author Mark Wyszomierski (markww@gmail.com), foursquare.
 */
public class FriendSearchInviteNonFoursquareUserAdapter extends BaseAdapter 
    implements ObservableAdapter {

    private LayoutInflater mInflater;
    private int mLayoutToInflate;
    private ButtonRowClickHandler mClickListener;
    
    private List<ContactSimple> mEmailsAndNames;

    public FriendSearchInviteNonFoursquareUserAdapter(
            Context context,
            ButtonRowClickHandler clickListener) {
        super();
        mInflater = LayoutInflater.from(context);
        mLayoutToInflate = R.layout.add_friends_invite_non_foursquare_user_list_item;
        mClickListener = clickListener;
        mEmailsAndNames = new ArrayList<ContactSimple>();
    }

    public void removeObserver() {
    }

    public FriendSearchInviteNonFoursquareUserAdapter(Context context, int layoutResource) {
        super();
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
            //holder.clickable = (LinearLayout) convertView
            //        .findViewById(R.id.addFriendListItemClickableArea);
            holder.name = (TextView) convertView.findViewById(R.id.addFriendNonFoursquareUserListItemName);
            holder.email = (TextView) convertView.findViewById(R.id.addFriendNonFoursquareUserListItemEmail);
            holder.add = (Button) convertView.findViewById(R.id.addFriendNonFoursquareUserListItemBtn);

            convertView.setTag(holder);

            //holder.clickable.setOnClickListener(mOnClickListenerInfo);
            holder.add.setOnClickListener(mOnClickListenerInvite);
        } else {
            // Get the ViewHolder back to get fast access to the TextView
            // and the ImageView.
            holder = (ViewHolder) convertView.getTag();
        }

        //holder.clickable.setTag(new Integer(position));
        holder.name.setText(mEmailsAndNames.get(position).mName);
        holder.email.setText(mEmailsAndNames.get(position).mEmail);
        holder.add.setTag(new Integer(position));

        return convertView;
    }

    private OnClickListener mOnClickListenerInvite = new OnClickListener() {
        @Override
        public void onClick(View v) {
            Integer position = (Integer) v.getTag();
            mClickListener.onBtnClickInvite((ContactSimple) getItem(position));
        }
    };

    public void removeItem(int position) throws IndexOutOfBoundsException {
        mEmailsAndNames.remove(position);
        notifyDataSetInvalidated();
    }

    public void setContacts(List<ContactSimple> contacts) {
        mEmailsAndNames = contacts;
    }

    static class ViewHolder {
        //LinearLayout clickable;
        TextView name;
        TextView email;
        Button add;
    }

    public interface ButtonRowClickHandler {
        public void onBtnClickInvite(ContactSimple contact);
        public void onInfoAreaClick(ContactSimple contact);
    }

    @Override
    public int getCount() {
        return mEmailsAndNames.size();
    }

    @Override
    public Object getItem(int position) {
        return mEmailsAndNames.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
}
