package vn.edu.usth.usthspeechrecord.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import vn.edu.usth.usthspeechrecord.R;
import vn.edu.usth.usthspeechrecord.models.Category;

public class CategoryAdapter extends BaseAdapter {

    Context mContext;
    int mLayout;
    List<Category> arrayList;
    String TAG="CategoryAdapter";

    public CategoryAdapter(Context context, int layout, List<Category> arrayList) {
        this.mContext = context;
        this.mLayout = layout;
        this.arrayList = arrayList;
    }

    public void setArrayList(List<Category> arrayList) {
        this.arrayList = arrayList;
        Log.e(TAG, "setArrayList: "+arrayList.size() );
    }

    @Override
    public int getCount() {
        return arrayList.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = inflater.inflate(mLayout, null);

        TextView txtName = convertView.findViewById(R.id.category_name);

        txtName.setText(arrayList.get(position).getCatName());
        Log.e(TAG, "getView: "+arrayList.get(position).getCatName());
        return convertView;
    }
}
