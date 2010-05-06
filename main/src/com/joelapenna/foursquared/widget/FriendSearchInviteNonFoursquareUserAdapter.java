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
    private AdapterListener mAdapterListener;
    
    private List<ContactSimple> mEmailsAndNames;

    
    public FriendSearchInviteNonFoursquareUserAdapter(
            Context context,
            AdapterListener adapterListener) {
        super();
        mInflater = LayoutInflater.from(context);
        mLayoutToInflate = R.layout.add_friends_invite_non_foursquare_user_list_item;
        mAdapterListener = adapterListener;
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

        if (position == 0) {
            ViewHolderInviteAll holder;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.add_friends_invite_non_foursquare_all_list_item, null);
                holder = new ViewHolderInviteAll();
                holder.addAll = (Button) convertView.findViewById(R.id.addFriendNonFoursquareAllListItemBtn);
                
                convertView.setTag(holder);
                
            } else {
                holder = (ViewHolderInviteAll) convertView.getTag();
            }
            
            holder.addAll.setOnClickListener(mOnClickListenerInviteAll);
        }
        else {
            ViewHolder holder;
            if (convertView == null) {
                convertView = mInflater.inflate(mLayoutToInflate, null);
    
                // Creates a ViewHolder and store references to the two children
                // views we want to bind data to.
                holder = new ViewHolder();
                holder.name = (TextView) convertView.findViewById(R.id.addFriendNonFoursquareUserListItemName);
                holder.email = (TextView) convertView.findViewById(R.id.addFriendNonFoursquareUserListItemEmail);
                holder.add = (Button) convertView.findViewById(R.id.addFriendNonFoursquareUserListItemBtn);
    
                convertView.setTag(holder);
    
                holder.add.setOnClickListener(mOnClickListenerInvite);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.name.setText(mEmailsAndNames.get(position - 1).mName);
            holder.email.setText(mEmailsAndNames.get(position - 1).mEmail);
            holder.add.setTag(new Integer(position));
        }

        return convertView;
    }

    private OnClickListener mOnClickListenerInvite = new OnClickListener() {
        @Override
        public void onClick(View v) {
            Integer position = (Integer) v.getTag();
            mAdapterListener.onBtnClickInvite((ContactSimple) getItem(position));
        }
    };

    private OnClickListener mOnClickListenerInviteAll = new OnClickListener() {
        @Override
        public void onClick(View v) {
            mAdapterListener.onInviteAll();
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
        TextView name;
        TextView email;
        Button add;
    }
    
    static class ViewHolderInviteAll {
        Button addAll;
    }

    public interface AdapterListener {
        public void onBtnClickInvite(ContactSimple contact);
        public void onInfoAreaClick(ContactSimple contact);
        public void onInviteAll();
    }

    @Override
    public int getCount() {
        if (mEmailsAndNames.size() > 0) {
            return mEmailsAndNames.size() + 1;
        }
        return 0;
    }

    @Override
    public Object getItem(int position) {
        if (position == 0) {
            return "";
        }
        return mEmailsAndNames.get(position - 1);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
    
    @Override
    public int getViewTypeCount() {
        return 2;
    }
    
    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return 0;
        }
        return 1;
    }
}
