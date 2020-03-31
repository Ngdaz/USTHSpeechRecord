package vn.edu.usth.usthspeechrecord.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import vn.edu.usth.usthspeechrecord.R;
import vn.edu.usth.usthspeechrecord.models.Category;
import vn.edu.usth.usthspeechrecord.models.MainCategory;

public class MainCategoryAdapter extends BaseAdapter {

    Context mContext;
    int mLayout;
    List<MainCategory> arrayList;

    public MainCategoryAdapter(Context context, int layout, List<MainCategory> arrayList) {
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

        txtName.setText(arrayList.get(position).getName());
        return convertView;
    }
}
