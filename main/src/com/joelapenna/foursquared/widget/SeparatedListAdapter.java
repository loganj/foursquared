/* Copyright 2008 Jeff Sharkey
 *
 *  From: http://www.jsharkey.org/blog/2008/08/18/separating-lists-with-headers-in-android-09/
 */

package com.joelapenna.foursquared.widget;

import com.joelapenna.foursquared.R;

import android.content.Context;
import android.database.DataSetObserver;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;

import java.util.LinkedHashMap;
import java.util.Map;

public class SeparatedListAdapter extends BaseAdapter implements ObservableAdapter {

    public final Map<String, Adapter> sections = new LinkedHashMap<String, Adapter>();
    public final ArrayAdapter<String> headers;
    public final static int TYPE_SECTION_HEADER = 0;

    public SeparatedListAdapter(Context context) {
        super();
        headers = new ArrayAdapter<String>(context, R.layout.list_header);
    }

    public SeparatedListAdapter(Context context, int layoutId) {
        super();
        headers = new ArrayAdapter<String>(context, layoutId);
    }

    public void addSection(String section, Adapter adapter) {
        this.headers.add(section);
        this.sections.put(section, adapter);
        
        // Register an observer so we can call notifyDataSetChanged() when our
        // children adapters are modified, otherwise no change will be visible.
        adapter.registerDataSetObserver(mDataSetObserver);
    }
    
    public void removeObserver() {
        // Notify all our children that they should release their observers too.
        for (Map.Entry<String, Adapter> it : sections.entrySet()) {
            if (it.getValue() instanceof ObservableAdapter) {
                ObservableAdapter adapter = (ObservableAdapter)it.getValue();
                adapter.removeObserver();
            }
        }
    }

    public void clear() {
        headers.clear();
        sections.clear();
        notifyDataSetInvalidated();
    }

    @Override
    public Object getItem(int position) {
        for (Object section : this.sections.keySet()) {
            Adapter adapter = sections.get(section);
            int size = adapter.getCount() + 1;

            // check if position inside this section
            if (position == 0) return section;
            if (position < size) return adapter.getItem(position - 1);

            // otherwise jump into next section
            position -= size;
        }
        return null;
    }

    @Override
    public int getCount() {
        // total together all sections, plus one for each section header
        int total = 0;
        for (Adapter adapter : this.sections.values())
            total += adapter.getCount() + 1;
        return total;
    }

    @Override
    public int getViewTypeCount() {
        // assume that headers count as one, then total all sections
        int total = 1;
        for (Adapter adapter : this.sections.values())
            total += adapter.getViewTypeCount();
        return total;
    }

    @Override
    public int getItemViewType(int position) {
        int type = 1;
        for (Object section : this.sections.keySet()) {
            Adapter adapter = sections.get(section);
            int size = adapter.getCount() + 1;

            // check if position inside this section
            if (position == 0) return TYPE_SECTION_HEADER;
            if (position < size) return type + adapter.getItemViewType(position - 1);

            // otherwise jump into next section
            position -= size;
            type += adapter.getViewTypeCount();
        }
        return -1;
    }

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override
    public boolean isEnabled(int position) {
        return (getItemViewType(position) != TYPE_SECTION_HEADER);
    }

    @Override
    public boolean isEmpty() {
        return getCount() == 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        int sectionnum = 0;
        for (Object section : this.sections.keySet()) {
            Adapter adapter = sections.get(section);
            int size = adapter.getCount() + 1;

            if (position == 0) return headers.getView(sectionnum, convertView, parent);
            if (position < size) return adapter.getView(position - 1, convertView, parent);

            // otherwise jump into next section
            position -= size;
            sectionnum++;
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }
    
    private DataSetObserver mDataSetObserver = new DataSetObserver() {
        @Override
        public void onChanged() {
            notifyDataSetChanged();   
        }
    };
}
