package com.bkromhout.balances.adapters;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.bkromhout.balances.R;
import com.bkromhout.balances.data.models.Category;
import com.bkromhout.balances.events.CategoryClickEvent;
import com.bkromhout.rrvl.RealmRecyclerViewAdapter;
import io.realm.RealmResults;
import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;

/**
 * Adapter class which handles binding {@link Category}s to a RealmRecyclerView.
 */
public class CategoryAdapter extends RealmRecyclerViewAdapter<Category, RecyclerView.ViewHolder> {
    private final Context context;

    /**
     * Create a new {@link CategoryAdapter}.
     * @param context      Context.
     * @param realmResults Results of the Realm query to display.
     */
    public CategoryAdapter(Context context, RealmResults<Category> realmResults) {
        super(context, realmResults);
        this.context = context;
        setHasStableIds(true);
    }

    /**
     * Get the UIDs of the selected items.
     * @return Long array containing UIDs of selected items.
     */
    public Long[] getSelectedItemUids() {
        ArrayList<Long> uids = new ArrayList<>(selectedPositions.size());
        for (int selectedPos : selectedPositions) {
            uids.add((Long) ids.get(selectedPos));
        }
        return uids.toArray(new Long[uids.size()]);
    }

    @Override
    public long getItemId(int position) {
        final Category category = realmResults.get(position);
        return category.isValid() ? category.uniqueId : -1;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new CategoryVH(inflater.inflate(R.layout.category_item, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        CategoryVH vh = (CategoryVH) holder;
        final Category category = realmResults.get(position);
        if (!category.isValid()) return;

        // Visually distinguish selected items during multi-select mode.
        vh.content.setSelected(isSelected(position));

        // Set long click handler.
        vh.content.setOnLongClickListener(view -> {
            EventBus.getDefault().post(new CategoryClickEvent(CategoryClickEvent.Type.LONG, category.uniqueId,
                    vh.getAdapterPosition(), vh.getLayoutPosition()));
            return true;
        });

        // Set edit button click handler.
        vh.btnEdit.setOnClickListener(view ->
                EventBus.getDefault().post(new CategoryClickEvent(CategoryClickEvent.Type.EDIT, category.uniqueId,
                        vh.getAdapterPosition(), vh.getLayoutPosition())));

        // Set data.
        vh.tvCategoryName.setText(category.name);
        vh.tvCategoryType.setText(category.isCredit ? R.string.credit : R.string.debit);
        vh.tvCategoryType.setTextColor(
                ContextCompat.getColor(context, category.isCredit ? R.color.textColorGreen : R.color.textColorRed));
    }

    static class CategoryVH extends RecyclerView.ViewHolder {
        @BindView(R.id.content)
        ViewGroup content;
        @BindView(R.id.category_name)
        TextView tvCategoryName;
        @BindView(R.id.balance_amount)
        TextView tvCategoryType;
        @BindView(R.id.edit_category)
        ImageButton btnEdit;

        CategoryVH(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            btnEdit.setVisibility(View.VISIBLE);
        }
    }
}
