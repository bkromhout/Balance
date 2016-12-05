package com.bkromhout.balances.adapters;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.bkromhout.balances.R;
import com.bkromhout.balances.data.models.Category;
import com.bkromhout.balances.data.models.CategoryFields;
import io.realm.Case;
import io.realm.RealmResults;

import java.util.List;

/**
 * Simple adapter which allows us to populate an AutoCompleteTextView with {@link Category} items. Filters using Realm's
 * {@code beginWith()} method.
 */
public class CategoryStartsWithAdapter extends FilterableRealmBaseAdapter<Category> {
    public CategoryStartsWithAdapter(Context context, @LayoutRes int layout, RealmResults<Category> realmObjectList) {
        super(context, layout, realmObjectList);
    }

    @Override
    List<Category> performRealmFiltering(@NonNull CharSequence constraint, RealmResults<Category> results) {
        // Return everything right away if our length is 0.
        if (constraint.length() == 0)
            return results;
        // Filter by Categories which begin with the constraint (case-insensitive).
        return results.where()
                      .beginsWith(CategoryFields.NAME, constraint.toString(), Case.INSENSITIVE)
                      .findAllSorted(CategoryFields.NAME);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        Category category = mResults.get(position);

        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.category_item, parent, false);
        }

        TextView tvCategoryName = (TextView) convertView.findViewById(R.id.category_name);
        TextView tvCategoryType = (TextView) convertView.findViewById(R.id.category_type);

        tvCategoryName.setText(category.name);
        tvCategoryType.setText(category.isCredit ? R.string.credit : R.string.debit);
        tvCategoryType.setTextColor(ContextCompat.getColor(
                getContext(), category.isCredit ? R.color.textColorGreen : R.color.textColorRed));

        return convertView;
    }
}
