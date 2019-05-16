package vn.edu.usth.usthspeechrecord;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

public class CategoryAdapter extends BaseAdapter {

    Context mContext;
    int mLayout;
    List<Category> arrayList;

    public CategoryAdapter(Context context, int layout, List<Category> arrayList) {
        this.mContext = context;
        this.mLayout = layout;
        this.arrayList = arrayList;
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
        return convertView;
    }
}
